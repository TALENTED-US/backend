package com.talented.buttie.mydata.mapper;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.mydata.domain.MerchantCategoryMappingType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantCategoryMappingMapper {

    ExpenseCategory findActiveExpenseCategory(
        @Param("mappingType") MerchantCategoryMappingType mappingType,
        @Param("mappingValue") String mappingValue
    );
}
