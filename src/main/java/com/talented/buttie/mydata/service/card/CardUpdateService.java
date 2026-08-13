package com.talented.buttie.mydata.service.card;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.exception.CardErrorCode;
import com.talented.buttie.mydata.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardUpdateService {

    private final CardMapper cardMapper;

    @Transactional
    public CardVO update(CardVO card) {
        if (cardMapper.update(card) != 1) {
            throw ApplicationException.from(CardErrorCode.CARD_UPDATE_FAILED);
        }
        return card;
    }
}
