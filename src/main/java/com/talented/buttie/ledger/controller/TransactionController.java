package com.talented.buttie.ledger.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.CreateTransactionRequest;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionRequest;
import com.talented.buttie.ledger.dto.response.GetSumFixedExpenseResponse;
import com.talented.buttie.ledger.dto.response.fixed.FixedExpenseDetailResponse;
import com.talented.buttie.ledger.dto.response.transaction.TransactionDetailResponse;
import com.talented.buttie.ledger.dto.response.transaction.TransactionResponse;
import com.talented.buttie.ledger.service.GetSumFixedExpenseService;
import com.talented.buttie.ledger.service.fixed.CreateFixedExpenseService;
import com.talented.buttie.ledger.service.transaction.CreateTransactionService;
import com.talented.buttie.ledger.service.fixed.DeleteFixedExpenseService;
import com.talented.buttie.ledger.service.transaction.DeleteTransactionService;
import com.talented.buttie.ledger.service.fixed.ReadFixedExpenseDetailService;
import com.talented.buttie.ledger.service.transaction.ReadTransactionDetailService;
import com.talented.buttie.ledger.service.transaction.ReadTransactionService;
import com.talented.buttie.ledger.service.transaction.UpdateTransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Api(tags = "Transactions")
public class TransactionController {
    private final ReadTransactionService readTransactionService;
    private final CreateTransactionService createTransactionService;
    private final UpdateTransactionService updateTransactionService;
    private final DeleteTransactionService deleteTransactionService;
    private final ReadTransactionDetailService readTransactionDetailService;
    private final ReadFixedExpenseDetailService readFixedExpenseDetailService;
    private final CreateFixedExpenseService createFixedExpenseService;
    private final DeleteFixedExpenseService deleteFixedExpenseService;
    private final GetSumFixedExpenseService getSumFixedExpenseService;

    @ApiOperation("거래 목록 조회")
    @GetMapping("")
    public ApplicationResponse<List<TransactionResponse>> getAllTransactions(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        List<TransactionVO> transactions = readTransactionService.getAllTransactions(targetUserId);

        List<TransactionResponse> responseList = transactions.stream()
            .map(TransactionResponse::from)
            .toList();

        return ApplicationResponse.onSuccess(responseList);
    }

    @ApiOperation("수동 거래 내역 등록")
    @PostMapping("")
    public ApplicationResponse<Long> createTransaction(
        @AuthUser AuthenticationUser user,

        @Valid @RequestBody CreateTransactionRequest request
    ){
        Long targetUserId = user.userId();
        Long transactionId = createTransactionService.createTransaction(targetUserId, request);

        return ApplicationResponse.onSuccess(transactionId);
    }

    @ApiOperation("수동 거래 내역 수정 (외부거래 ID가 없을 때 사용)")
    @PatchMapping("/{transactionId}")
    public ApplicationResponse<TransactionResponse> updateTransaction(
        @PathVariable Long transactionId,
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody UpdateTransactionRequest request
    ){
        Long targetUserId = user.userId();
        TransactionVO transaction = updateTransactionService.updateTransaction(targetUserId, transactionId, request);
        return ApplicationResponse.onSuccess(TransactionResponse.from(transaction));
    }

    @ApiOperation("외부 거래 메모 수정 (외부거래 ID가 있을 때 사용)")
    @PatchMapping("/{transactionId}/memo")
    public ApplicationResponse<TransactionResponse> updateTransactionMemo(
        @PathVariable Long transactionId,
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody UpdateTransactionMemoRequest request
    ){
        Long targetUserId = user.userId();
        TransactionVO transactionMemo = updateTransactionService.updateTransactionMemo(targetUserId, transactionId, request);
        return ApplicationResponse.onSuccess(TransactionResponse.from(transactionMemo));
    }

    @ApiOperation("수동 거래 내역 삭제 (외부거래 ID가 없을 때 사용)")
    @DeleteMapping("/{transactionId}")
    public ApplicationResponse<Long> deleteTransaction(
        @PathVariable Long transactionId,
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        Long deleteUserId = deleteTransactionService.deleteTransaction(targetUserId, transactionId);
        return ApplicationResponse.onSuccess(deleteUserId);
    }

    @ApiOperation("고정 지출 상세 목록 조회")
    @GetMapping("/fixed")
    public ApplicationResponse<List<FixedExpenseDetailResponse>> getFixedExpenseDetails(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        List<TransactionVO> fixedExpenses = readFixedExpenseDetailService.getFixedExpenseDetails(targetUserId);

        List<FixedExpenseDetailResponse> responseList = fixedExpenses.stream()
            .map(FixedExpenseDetailResponse::from)
            .toList();

        return ApplicationResponse.onSuccess(responseList);
    }

    @ApiOperation("지난달 및 이번달 고정 지출 금액 합계 조회")
    @GetMapping("/fixed/sum")
    public ApplicationResponse<GetSumFixedExpenseResponse> getSumFixedExpense(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        List<TransactionVO> fixedExpenses = getSumFixedExpenseService.getFixedExpenses(targetUserId);

        YearMonth currentYearMonth = YearMonth.now();
        YearMonth lastYearMonth = currentYearMonth.minusMonths(1);

        // 1. 이번달 고정 지출 합계 (이번달 날짜에 해당하는 고정 지출 AMOUNT 합계)
        int currentMonthTotal = fixedExpenses.stream()
            .filter(tx -> tx.getTransactionAt() != null && YearMonth.from(tx.getTransactionAt()).equals(currentYearMonth))
            .map(TransactionVO::getTransactionAmount)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        // 2. 지난달 고정 지출 합계 (지난달 날짜에 해당하는 고정 지출 AMOUNT 합계)
        int lastMonthTotal = fixedExpenses.stream()
            .filter(tx -> tx.getTransactionAt() != null && YearMonth.from(tx.getTransactionAt()).equals(lastYearMonth))
            .map(TransactionVO::getTransactionAmount)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        GetSumFixedExpenseResponse response = GetSumFixedExpenseResponse.of(lastMonthTotal, currentMonthTotal);
        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("거래 상세 조회")
    @GetMapping("/{transactionId}")
    public ApplicationResponse<TransactionDetailResponse> getTransactionDetail(
        @PathVariable("transactionId") Long transactionId,
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        TransactionDetailResponse response = readTransactionDetailService.getTransactionDetail(targetUserId, transactionId);
        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("고정 지출 추가")
    @PatchMapping("/{transactionId}/fixed")
    public ApplicationResponse<Long> createFixedExpense(
        @PathVariable("transactionId") String transactionId,
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        Long decryptedTransactionId = PKCrypto.decrypt(transactionId);
        Long resultTransactionId = createFixedExpenseService.createFixedExpense(targetUserId, decryptedTransactionId);

        return ApplicationResponse.onSuccess(resultTransactionId);
    }

    @ApiOperation("고정 지출 삭제(해제)")
    @PatchMapping("/{transactionId}/fixed/delete")
    public ApplicationResponse<Long> deleteFixedExpense(
        @PathVariable("transactionId") String transactionId,
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        Long decryptedTransactionId = PKCrypto.decrypt(transactionId);
        Long resultTransactionId = deleteFixedExpenseService.deleteFixedExpense(targetUserId, decryptedTransactionId);

        return ApplicationResponse.onSuccess(resultTransactionId);
    }
}
