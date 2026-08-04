package com.talented.buttie.ledger.domain;

public enum ExpenseCategory {
    FOOD("식비"),
    TRANSPORT("교통비"),
    HOUSING("주거비"),
    COMMUNICATION("통신비"),
    SUBSCRIPTION("구독비"),
    EDUCATION("교육비"),
    CERTIFICATE("자격증 비용"),
    ETC_EXPENSE("기타 비용");

    private final String value;

    ExpenseCategory(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
