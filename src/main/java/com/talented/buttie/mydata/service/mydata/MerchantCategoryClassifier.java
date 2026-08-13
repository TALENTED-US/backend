package com.talented.buttie.mydata.service.mydata;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.domain.MerchantCategoryMappingType;
import com.talented.buttie.mydata.mapper.MerchantCategoryMappingMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MerchantCategoryClassifier {

    private final MerchantCategoryMappingMapper merchantCategoryMappingMapper;

    private final Cache<String, Optional<ExpenseCategory>> merchantCategoryCache;

    //Qualifier 어노테이션을 사용하여 "merchantCategoryCache"라는 이름의 캐시 빈을 주입받습니다.
    //@RequiredArgsConstructor를 사용하지 않고 생성자를 직접 정의하여 의존성을 주입받습니다.
    public MerchantCategoryClassifier(
        MerchantCategoryMappingMapper merchantCategoryMappingMapper,
        @Qualifier("merchantCategoryCache") Cache<String, Optional<ExpenseCategory>> merchantCategoryCache
    ) {
        this.merchantCategoryMappingMapper = merchantCategoryMappingMapper;
        this.merchantCategoryCache = merchantCategoryCache;
    }

    public ClassificationResult classify(MydataCardApprovalData approval) {
        Optional<ExpenseCategory> byRegistrationNumber = findCategory(
            MerchantCategoryMappingType.MERCHANT_REGNO,
            approval.getMerchantRegistrationNumber()
        );
        if (byRegistrationNumber.isPresent()) {
            return new ClassificationResult(
                byRegistrationNumber.get(),
                ClassificationMethod.MERCHANT_REGNO
            );
        }

        Optional<ExpenseCategory> byMerchantCode = findCategory(
            MerchantCategoryMappingType.MERCHANT_CATEGORY_CODE,
            approval.getMerchantCategoryCode()
        );
        return byMerchantCode.map(expenseCategory -> new ClassificationResult(
            expenseCategory,
            ClassificationMethod.MERCHANT_CATEGORY_CODE
        )).orElseGet(() -> new ClassificationResult(
            ExpenseCategory.OTHER_FINANCE,
            ClassificationMethod.UNCLASSIFIED
        ));

    }

    public void clearCache() {
        merchantCategoryCache.invalidateAll();
    }

    private Optional<ExpenseCategory> findCategory(
        MerchantCategoryMappingType mappingType,
        String mappingValue
    ) {
        if (mappingValue == null || mappingValue.isBlank()) {
            return Optional.empty();
        }

        String cacheKey = mappingType.name() + ":" + mappingValue.trim();
        return merchantCategoryCache.get(
            cacheKey,
            key -> Optional.ofNullable(
                merchantCategoryMappingMapper.findActiveExpenseCategory(
                    mappingType,
                    mappingValue.trim()
                )
            )
        );
    }

    public record ClassificationResult(
        ExpenseCategory category,
        ClassificationMethod method
    ) {

    }
}
