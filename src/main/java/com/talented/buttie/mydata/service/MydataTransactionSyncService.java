package com.talented.buttie.mydata.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataTransactionSyncService {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataConnectionValidator mydataConnectionValidator;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final MydataTransactionImportService mydataTransactionImportService;
    private final MydataDuplicateTransactionService mydataDuplicateTransactionService;
    private final FixedExpenseCandidateService fixedExpenseCandidateService;
    private final MydataConnectionMapper mydataConnectionMapper;

    @Transactional
    public MydataTransactionSyncResponse syncAndAnalyze(Long userId) {
        mydataConnectionValidator.validateConnected(userId);
        List<AccountVO> accounts = accountMapper.findActiveByUserId(userId);
        List<CardVO> cards = cardMapper.findActiveByUserId(userId);
        if (accounts.isEmpty() && cards.isEmpty()) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ASSET_NOT_REGISTERED);
        }

        MydataTransactionImportService.SyncResult syncResult =
            mydataTransactionImportService.sync(
                userId,
                new MydataAssetRegistrationService.RegisteredAssets(accounts, cards)
            );
        LocalDateTime lastSyncedAt = LocalDateTime.now();
        if (mydataConnectionMapper.updateLastSyncedAt(
            userId,
            MOCK_PROVIDER,
            lastSyncedAt
        ) != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED);
        }
        int excludedDuplicateCount =
            mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(userId);
        List<FixedExpenseCandidateResponse> candidates =
            fixedExpenseCandidateService.findCandidates(userId);

        return MydataTransactionSyncResponse.builder()
            .insertedTransactionCount(syncResult.insertedCount())
            .skippedTransactionCount(syncResult.skippedCount())
            .excludedDuplicateCount(excludedDuplicateCount)
            .fixedExpenseCandidateCount(candidates.size())
            .lastSyncedAt(lastSyncedAt)
            .build();
    }
}
