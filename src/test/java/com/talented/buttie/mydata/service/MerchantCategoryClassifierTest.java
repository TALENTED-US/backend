package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import org.junit.jupiter.api.Test;

class MerchantCategoryClassifierTest {

    private final MerchantCategoryClassifier classifier = new MerchantCategoryClassifier();

    @Test
    void 사업자등록번호를_우선해_카드_지출을_분류한다() {
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("900-00-00003")
            .merchantCategoryCode("5812")
            .build();

        MerchantCategoryClassifier.ClassificationResult result = classifier.classify(approval);

        assertEquals(ExpenseCategory.SUBSCRIPTION, result.category());
        assertEquals(ClassificationMethod.MERCHANT_REGNO, result.method());
    }

    @Test
    void 등록번호를_모르면_가맹점업종코드로_분류한다() {
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("unknown")
            .merchantCategoryCode("4111")
            .build();

        MerchantCategoryClassifier.ClassificationResult result = classifier.classify(approval);

        assertEquals(ExpenseCategory.TRANSPORT, result.category());
        assertEquals(ClassificationMethod.MERCHANT_CATEGORY_CODE, result.method());
    }
}
