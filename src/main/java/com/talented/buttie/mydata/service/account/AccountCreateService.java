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
public class AccountCreateService {

    private final AccountMapper accountMapper;

    @Transactional
    public AccountVO create(AccountVO account) {
        if (accountMapper.insert(account) != 1) {
            throw ApplicationException.from(AccountErrorCode.ACCOUNT_CREATE_FAILED);
        }
        return account;
    }
}
