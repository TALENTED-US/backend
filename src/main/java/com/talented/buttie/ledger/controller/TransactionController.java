package com.talented.buttie.ledger.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.RegisterTransactionRequest;
import com.talented.buttie.ledger.dto.request.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequest;
import com.talented.buttie.ledger.dto.response.TransactionResponse;
import com.talented.buttie.ledger.service.GetTransactionService;
import com.talented.buttie.ledger.service.RegisterTransactionService;
import com.talented.buttie.ledger.service.UpdateTransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final GetTransactionService getTransactionService;
    private final RegisterTransactionService registerTransactionService;
    private final UpdateTransactionService updateTransactionService;

    @ApiOperation("거래 목록 조회")
    @GetMapping("")
    public ApplicationResponse<List<TransactionResponse>> getAllTransactions(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        List<TransactionVO> transactions = getTransactionService.getAllTransactions(targetUserId);

        return ApplicationResponse.onSuccess(TransactionResponse.fromList(transactions));
    }

    @ApiOperation("수동 거래 내역 등록")
    @PostMapping("")
    public ApplicationResponse<TransactionResponse> registerTransaction(
        @AuthUser AuthenticationUser user,

        @ApiParam(value = "수동 거래 추가 정보", required = true)
        @Valid @RequestBody RegisterTransactionRequest request
    ){
        Long targetUserId = user.userId();
        TransactionVO transaction = registerTransactionService.registerTransaction(targetUserId, request);

        TransactionResponse responseDTO = TransactionResponse.from(transaction);

        return ApplicationResponse.onSuccess(responseDTO);
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
}
