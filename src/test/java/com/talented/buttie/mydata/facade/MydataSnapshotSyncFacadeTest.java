package com.talented.buttie.mydata.facade;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.service.mydata.MydataTransactionSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataSnapshotSyncFacadeTest {

    @Mock
    private MydataTransactionSyncService mydataTransactionSyncService;
    @Test
    void 외부_거래_수집과_영속화_과정을_위임한다() {
        MydataSnapshotSyncFacade facade = new MydataSnapshotSyncFacade(mydataTransactionSyncService);
        MydataTransactionSyncResponse response = MydataTransactionSyncResponse.builder().build();
        when(mydataTransactionSyncService.syncAndAnalyze(1L)).thenReturn(response);

        MydataTransactionSyncResponse result = facade.syncAndRefreshSnapshot(1L);

        assertSame(response, result);
        verify(mydataTransactionSyncService).syncAndAnalyze(1L);
    }
}
