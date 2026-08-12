package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.dto.response.MydataAssetRegistrationResponse;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataOnboardingServiceTest {

    @Mock
    private MydataAssetRegistrationService mydataAssetRegistrationService;
    @Mock
    private MydataTransactionImportService mydataTransactionImportService;
    @Mock
    private MydataDuplicateTransactionService mydataDuplicateTransactionService;
    @Mock
    private FixedExpenseCandidateService fixedExpenseCandidateService;
    @Mock
    private MydataConnectionMapper mydataConnectionMapper;
    @InjectMocks
    private MydataOnboardingService service;

    @Test
    void 거래_동기화가_성공하면_마지막_동기화_시각을_저장한다() {
        RegisterMydataAssetsRequest request = new RegisterMydataAssetsRequest(
            List.of("ACCOUNT-1"),
            List.of("CARD-1")
        );
        MydataAssetRegistrationService.RegisteredAssets assets =
            new MydataAssetRegistrationService.RegisteredAssets(
                List.of(AccountVO.builder().accountId(1L).build()),
                List.of(CardVO.builder().cardId(2L).build())
            );
        given(mydataAssetRegistrationService.registerSelectedAssets(101L, request))
            .willReturn(assets);
        given(mydataTransactionImportService.sync(101L, assets))
            .willReturn(new MydataTransactionImportService.SyncResult(3, 1));
        given(mydataConnectionMapper.updateLastSyncedAt(
            org.mockito.ArgumentMatchers.eq(101L),
            org.mockito.ArgumentMatchers.eq("MOCK"),
            any(LocalDateTime.class)
        )).willReturn(1);
        given(mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(101L))
            .willReturn(1);
        given(fixedExpenseCandidateService.findCandidates(101L)).willReturn(List.of());

        MydataAssetRegistrationResponse result = service.registerAndAnalyze(101L, request);

        assertEquals(3, result.insertedTransactionCount());
        verify(mydataConnectionMapper).updateLastSyncedAt(
            org.mockito.ArgumentMatchers.eq(101L),
            org.mockito.ArgumentMatchers.eq("MOCK"),
            any(LocalDateTime.class)
        );
    }
}
