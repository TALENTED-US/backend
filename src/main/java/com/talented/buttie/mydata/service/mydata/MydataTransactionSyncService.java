package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import com.talented.buttie.mydata.service.mydata.MydataAssetRegistrationService.RegisteredAssets;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataTransactionSyncService {

    private final MydataConnectionValidator mydataConnectionValidator;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final MydataTransactionImportService mydataTransactionImportService;
    private final MydataSyncPersistenceService mydataSyncPersistenceService;

    // @Transactional 제거: 외부 API 호출 구간이 커넥션을 점유하지 않도록 함.
    public MydataTransactionSyncResponse syncAndAnalyze(Long userId) {
        mydataConnectionValidator.validateConnected(userId);
        List<AccountVO> accounts = accountMapper.findActiveByUserId(userId);
        List<CardVO> cards = cardMapper.findActiveByUserId(userId);
        if (accounts.isEmpty() && cards.isEmpty()) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ASSET_NOT_REGISTERED);
        }

        // 1단계: 외부 API 호출 (DB 커넥션 미사용)
        List<TransactionVO> candidates =
            mydataTransactionImportService.fetchExternalTransactions(
                userId, new MydataAssetRegistrationService.RegisteredAssets(accounts, cards));

        // 2단계: DB 쓰기만 짧은 트랜잭션으로 (별도의 Bean이라 @Transactional 정상 적용됨)
        return mydataSyncPersistenceService.persistAndRefreshSnapshot(userId, candidates);
    }
}
