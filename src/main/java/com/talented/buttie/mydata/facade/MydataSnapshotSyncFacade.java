package com.talented.buttie.mydata.facade;

import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.service.mydata.MydataTransactionSyncService;
import com.talented.buttie.simulation.service.FinancialSnapshotCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataSnapshotSyncFacade {

    private final MydataTransactionSyncService mydataTransactionSyncService;
    private final FinancialSnapshotCreateService financialSnapshotCreateService;

    @Transactional
    public MydataTransactionSyncResponse syncAndRefreshSnapshot(Long userId) {
        MydataTransactionSyncResponse response = mydataTransactionSyncService.syncAndAnalyze(userId);
        financialSnapshotCreateService.createSnapshot(userId);
        return response;
    }
}
