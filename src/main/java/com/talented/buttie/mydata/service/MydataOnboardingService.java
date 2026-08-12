package com.talented.buttie.mydata.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetRegistrationResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataOnboardingService {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataAssetRegistrationService mydataAssetRegistrationService;
    private final MydataTransactionImportService mydataTransactionImportService;
    private final MydataDuplicateTransactionService mydataDuplicateTransactionService;
    private final FixedExpenseCandidateService fixedExpenseCandidateService;
    private final MydataConnectionMapper mydataConnectionMapper;

    @Transactional
    public MydataAssetRegistrationResponse registerAndAnalyze(
        Long userId,
        RegisterMydataAssetsRequest request
    ) {
        MydataAssetRegistrationService.RegisteredAssets assets =
            mydataAssetRegistrationService.registerSelectedAssets(userId, request);
        MydataTransactionImportService.SyncResult syncResult =
            mydataTransactionImportService.sync(userId, assets);
        if (mydataConnectionMapper.updateLastSyncedAt(
            userId,
            MOCK_PROVIDER,
            LocalDateTime.now()
        ) != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED);
        }
        int excludedDuplicateCount =
            mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(userId);
        List<FixedExpenseCandidateResponse> candidates =
            fixedExpenseCandidateService.findCandidates(userId);

        return MydataAssetRegistrationResponse.builder()
            .registeredAccountCount(assets.accounts().size())
            .registeredCardCount(assets.cards().size())
            .insertedTransactionCount(syncResult.insertedCount())
            .skippedTransactionCount(syncResult.skippedCount())
            .excludedDuplicateCount(excludedDuplicateCount)
            .fixedExpenseCandidateCount(candidates.size())
            .build();
    }
}
