package com.talented.buttie.mydata.facade;

import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.service.mydata.MydataTransactionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataSnapshotSyncFacade {

    private final MydataTransactionSyncService mydataTransactionSyncService;

    public MydataTransactionSyncResponse syncAndRefreshSnapshot(Long userId) {
        return mydataTransactionSyncService.syncAndAnalyze(userId);
    }
}
