package com.talented.buttie.mydata.service.account;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.exception.AccountErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountUpdateService {

    private final AccountMapper accountMapper;

    @Transactional
    public AccountVO update(AccountVO account) {
        if (accountMapper.update(account) != 1) {
            throw ApplicationException.from(AccountErrorCode.ACCOUNT_UPDATE_FAILED);
        }
        return account;
    }
}
