package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.domain.MerchantCategoryMappingType;
import com.talented.buttie.mydata.mapper.MerchantCategoryMappingMapper;
import com.talented.buttie.mydata.service.mydata.MerchantCategoryClassifier;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantCategoryClassifierTest {

    @Mock
    private MerchantCategoryMappingMapper merchantCategoryMappingMapper;

    private MerchantCategoryClassifier classifier;

    @BeforeEach
    void setUp() {
        Cache<String, Optional<ExpenseCategory>> cache = Caffeine.newBuilder().build();
        classifier = new MerchantCategoryClassifier(merchantCategoryMappingMapper, cache);
    }

    @Test
    void 사업자등록번호를_우선해_카드_지출을_분류한다() {
        given(merchantCategoryMappingMapper.findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            "900-00-00003"
        )).willReturn(ExpenseCategory.HOBBY_LEISURE);
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("900-00-00003")
            .merchantName("넷플릭스")
            .build();

        MerchantCategoryClassifier.ClassificationResult result = classifier.classify(approval);

        assertEquals(ExpenseCategory.HOBBY_LEISURE, result.category());
        assertEquals(ClassificationMethod.MERCHANT_REGNO, result.method());
    }

    @Test
    void 등록번호를_모르면_정규화한_가맹점명으로_분류한다() {
        given(merchantCategoryMappingMapper.findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            "unknown"
        )).willReturn(null);
        given(merchantCategoryMappingMapper.findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_NAME,
            "NOL 인터파크"
        )).willReturn(ExpenseCategory.HOBBY_LEISURE);
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("unknown")
            .merchantName("  nol   인터파크  ")
            .build();

        MerchantCategoryClassifier.ClassificationResult result = classifier.classify(approval);

        assertEquals(ExpenseCategory.HOBBY_LEISURE, result.category());
        assertEquals(ClassificationMethod.MERCHANT_NAME, result.method());
    }

    @Test
    void 페르소나용_주점_가맹점도_사업자등록번호로_분류한다() {
        given(merchantCategoryMappingMapper.findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            "900-00-00011"
        )).willReturn(ExpenseCategory.ALCOHOL_ENTERTAINMENT);
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("900-00-00011")
            .build();

        MerchantCategoryClassifier.ClassificationResult result = classifier.classify(approval);

        assertEquals(ExpenseCategory.ALCOHOL_ENTERTAINMENT, result.category());
        assertEquals(ClassificationMethod.MERCHANT_REGNO, result.method());
    }

    @Test
    void 같은_매핑은_캐시에서_조회해_DB를_한번만_호출한다() {
        given(merchantCategoryMappingMapper.findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            "900-00-00001"
        )).willReturn(ExpenseCategory.CAFE_SNACK);
        MydataCardApprovalData approval = MydataCardApprovalData.builder()
            .merchantRegistrationNumber("900-00-00001")
            .build();

        classifier.classify(approval);
        classifier.classify(approval);

        verify(merchantCategoryMappingMapper, times(1)).findActiveExpenseCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            "900-00-00001"
        );
    }
}
