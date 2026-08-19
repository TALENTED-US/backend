package com.talented.buttie.catalog.controller;

import com.talented.buttie.catalog.domain.PolicyCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PolicyCategoryConverter implements Converter<String, PolicyCategory> {

    @Override
    public PolicyCategory convert(String source) {
        return PolicyCategory.from(source);
    }
}
