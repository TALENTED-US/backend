package com.talented.buttie.mydata.service;

import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetRegistrationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataOnboardingService {

    private final MydataAssetRegistrationService mydataAssetRegistrationService;
    private final MydataTransactionImportService mydataTransactionImportService;
    private final MydataDuplicateTransactionService mydataDuplicateTransactionService;
    private final FixedExpenseCandidateService fixedExpenseCandidateService;

    @Transactional
    public MydataAssetRegistrationResponse registerAndAnalyze(
        Long userId,
        RegisterMydataAssetsRequest request
    ) {
        MydataAssetRegistrationService.RegisteredAssets assets =
            mydataAssetRegistrationService.registerSelectedAssets(userId, request);
        MydataTransactionImportService.SyncResult syncResult =
            mydataTransactionImportService.sync(userId, assets);
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
