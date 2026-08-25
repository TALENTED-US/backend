package com.talented.buttie.catalog.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum PolicyCategory {
    HOUSING("주거"),
    TRANSPORT("교통"),
    WELFARE("복지"),
    EMPLOYMENT("취업"),
    EDUCATION("교육"),
    YOUTH_SUPPORT("참여"),
    OTHER("기타");

    private final String value;

    PolicyCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PolicyCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
            .filter(category -> category.name().equalsIgnoreCase(value) || category.value.equals(value))
            .findFirst()
            .orElse(OTHER);
    }
}
