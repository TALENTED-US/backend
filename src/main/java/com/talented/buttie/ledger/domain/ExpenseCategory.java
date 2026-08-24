package com.talented.buttie.ledger.domain;

public enum ExpenseCategory {
    FOOD("식비"),
    ALCOHOL_ENTERTAINMENT("술, 유흥"),
    CAFE_SNACK("카페, 간식"),
    JOB_PREPARATION("취준 비용"),
    SHOPPING("쇼핑"),
    HOBBY_LEISURE("취미, 여가"),
    HOUSING_COMMUNICATION("주거, 통신"),
    TRANSPORT_FUEL("교통, 유류비"),
    HEALTH_FITNESS("의료, 건강, 피트니스"),
    OTHER_FINANCE("기타 금융");

    private final String value;

    ExpenseCategory(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public String toKoreanName() {
        return value.replace(", ", "·").replace(" ", "·");
    }

    public static String toKoreanName(ExpenseCategory category) {
        if (category == null) {
            return null;
        }
        return category.toKoreanName();
    }
}
