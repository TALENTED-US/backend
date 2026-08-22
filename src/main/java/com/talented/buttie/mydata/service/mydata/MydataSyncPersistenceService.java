package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataSyncPersistenceService {

    private static final String MOCK_PROVIDER = "MOCK";
    private final MydataTransactionImportService mydataTransactionImportService;
    private final MydataDuplicateTransactionService mydataDuplicateTransactionService;
    private final FixedExpenseCandidateService fixedExpenseCandidateService;
    private final MydataConnectionMapper mydataConnectionMapper;

    @Transactional
    public MydataTransactionSyncResponse persist(
        Long userId,
        List<TransactionVO> candidates
    ) {
        MydataTransactionImportService.SyncResult syncResult =
            mydataTransactionImportService.importTransactions(candidates);

        LocalDateTime lastSyncAt = LocalDateTime.now();
        if(mydataConnectionMapper.updateLastSyncedAt(userId, MOCK_PROVIDER, lastSyncAt) != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED);
        }

        int excludedDuplicateCount =
            mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(userId);
        List<FixedExpenseCandidateResponse> candidateResponses =
            fixedExpenseCandidateService.findCandidates(userId);

        return MydataTransactionSyncResponse.builder()
            .insertedTransactionCount(syncResult.insertedCount())
            .skippedTransactionCount(syncResult.skippedCount())
            .excludedDuplicateCount(excludedDuplicateCount)
            .fixedExpenseCandidateCount(candidateResponses.size())
            .lastSyncedAt(lastSyncAt)
            .build();
    }
}
