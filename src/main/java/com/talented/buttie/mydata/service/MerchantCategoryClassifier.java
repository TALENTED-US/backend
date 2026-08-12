package com.talented.buttie.mydata.service;

import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MerchantCategoryClassifier {

    private static final Map<String, ExpenseCategory> REGISTRATION_NUMBER_CATEGORIES = Map.ofEntries(
        Map.entry("900-00-00001", ExpenseCategory.FOOD),
        Map.entry("900-00-00002", ExpenseCategory.FOOD),
        Map.entry("900-00-00003", ExpenseCategory.SUBSCRIPTION),
        Map.entry("900-00-00004", ExpenseCategory.TRANSPORT),
        Map.entry("900-00-00005", ExpenseCategory.ETC_EXPENSE),
        Map.entry("900-00-00006", ExpenseCategory.FOOD),
        Map.entry("900-00-00007", ExpenseCategory.ETC_EXPENSE),
        Map.entry("900-00-00008", ExpenseCategory.TRANSPORT),
        Map.entry("900-00-00009", ExpenseCategory.EDUCATION),
        Map.entry("900-00-00010", ExpenseCategory.ETC_EXPENSE)
    );

    private static final Map<String, ExpenseCategory> MERCHANT_CODE_CATEGORIES = Map.ofEntries(
        Map.entry("5411", ExpenseCategory.FOOD),
        Map.entry("5812", ExpenseCategory.FOOD),
        Map.entry("5814", ExpenseCategory.FOOD),
        Map.entry("4111", ExpenseCategory.TRANSPORT),
        Map.entry("5541", ExpenseCategory.TRANSPORT),
        Map.entry("4899", ExpenseCategory.SUBSCRIPTION),
        Map.entry("5942", ExpenseCategory.EDUCATION)
    );

    public ClassificationResult classify(MydataCardApprovalData approval) {
        ExpenseCategory byRegistrationNumber = REGISTRATION_NUMBER_CATEGORIES.get(
            approval.getMerchantRegistrationNumber()
        );
        if (byRegistrationNumber != null) {
            return new ClassificationResult(
                byRegistrationNumber,
                ClassificationMethod.MERCHANT_REGNO
            );
        }

        ExpenseCategory byMerchantCode = MERCHANT_CODE_CATEGORIES.get(
            approval.getMerchantCategoryCode()
        );
        if (byMerchantCode != null) {
            return new ClassificationResult(
                byMerchantCode,
                ClassificationMethod.MERCHANT_CATEGORY_CODE
            );
        }

        return new ClassificationResult(
            ExpenseCategory.ETC_EXPENSE,
            ClassificationMethod.UNCLASSIFIED
        );
    }

    public record ClassificationResult(
        ExpenseCategory category,
        ClassificationMethod method
    ) {
    }
}
