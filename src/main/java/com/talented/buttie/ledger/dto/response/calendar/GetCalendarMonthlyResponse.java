package com.talented.buttie.ledger.dto.response.calendar;

import com.talented.buttie.ledger.domain.calendar.CalendarMonthlyVO;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.common.util.PKCrypto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

@ApiModel(description = "가계부 캘린더 월간 데이터 응답")
public record GetCalendarMonthlyResponse(

    @ApiModelProperty(value = "월 총 수입", example = "480000", required = true)
    int totalIncome,

    @ApiModelProperty(value = "월 총 지출", example = "628600", required = true)
    int totalExpense,

    @ApiModelProperty(value = "순현금흐름", example = "-148600", required = true)
    int netCashFlow,

    @ApiModelProperty(value = "카테고리별 지출 리스트 (도넛차트)", required = true)
    List<CategoryExpense> categoryExpenses,

    @ApiModelProperty(value = "거래 내역 리스트", required = true)
    List<Transaction> transactions
) {

    @ApiModel(description = "카테고리별 지출 요약")
    public record CategoryExpense(

        @ApiModelProperty(value = "지출 카테고리", example = "HOUSING", required = true)
        ExpenseCategory category,

        @ApiModelProperty(value = "해당 카테고리 총 지출", example = "500000", required = true)
        int amount
    ) {}

    @ApiModel(description = "거래 내역")
    public record Transaction(

        @ApiModelProperty(value = "암호화된 거래 ID", example = "xX79VwugC283X2XVQTkp1Q", required = true)
        String transactionId,

        @ApiModelProperty(value = "거래 일시", example = "2026-07-20T10:15:00", required = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime transactionAt,

        @ApiModelProperty(value = "거래 내용", example = "스타벅스 강남점", required = true)
        String transactionContent,

        @ApiModelProperty(value = "지출 카테고리", example = "FOOD", required = true)
        ExpenseCategory category,

        @ApiModelProperty(value = "거래 유형", example = "EXPENSE", required = true)
        TransactionType transactionType,

        @ApiModelProperty(value = "거래 금액", example = "6300", required = true)
        int amount,

        @ApiModelProperty(value = "거래 은행명", example = "KB국민은행")
        String institutionName
    ) {}

    public static GetCalendarMonthlyResponse from(CalendarMonthlyVO vo) {
        List<CategoryExpense> categoryExpenses = vo.getCategoryExpenses().stream()
            .map(c -> new CategoryExpense(c.getCategory(), c.getAmount()))
            .toList();

        List<Transaction> transactions = vo.getTransactions().stream()
            .map(t -> new Transaction(
                PKCrypto.encrypt(t.getTransactionId()),
                t.getTransactionAt(),
                t.getTransactionContent(),
                t.getCategory(),
                t.getTransactionType(),
                t.getAmount(),
                t.getInstitutionName()
            ))
            .toList();

        return new GetCalendarMonthlyResponse(
            vo.getTotalIncome(),
            vo.getTotalExpense(),
            vo.getNetCashFlow(),
            categoryExpenses,
            transactions
        );
    }
}