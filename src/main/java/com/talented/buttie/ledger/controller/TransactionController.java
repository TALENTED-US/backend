package com.talented.buttie.ledger.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.RegisterTransactionRequestDTO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionMemoRequestDTO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequestDTO;
import com.talented.buttie.ledger.dto.response.TransactionResponseDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApplicationResponse<List<TransactionResponseDTO>> getAllTransactions(
        @ApiParam(value= "사용자 ID", required = true)
        @RequestParam Long userId
    ){
        List<TransactionVO> transactions = getTransactionService.getAllTransactions(userId);

        return ApplicationResponse.onSuccess(TransactionResponseDTO.fromList(transactions));
    }

    @ApiOperation("수동 거래 내역 등록")
    @PostMapping("")
    public ApplicationResponse<TransactionResponseDTO> registerTransaction(
        @ApiParam(value = "사용자 ID", required = true)
        @RequestParam Long userId,

        @ApiParam(value = "수동 거래 추가 정보", required = true)
        @Valid @RequestBody RegisterTransactionRequestDTO request
    ){
        TransactionVO transaction = registerTransactionService.registerTransaction(userId, request);

        TransactionResponseDTO responseDTO = TransactionResponseDTO.from(transaction);

        return ApplicationResponse.onSuccess(responseDTO);
    }

    @ApiOperation("수동 거래 내역 수정 (외부거래 ID가 없을 때 사용)")
    @PatchMapping("/{transactionId}")
    public ApplicationResponse<TransactionResponseDTO> updateTransaction(
        @PathVariable Long transactionId,
        @RequestParam Long userId,
        @Valid @RequestBody UpdateTransactionRequestDTO request
    ){
        TransactionVO vo = updateTransactionService.updateTransaction(userId, transactionId, request);
        return ApplicationResponse.onSuccess(TransactionResponseDTO.from(vo));
    }

    @ApiOperation("외부 거래 메모 수정 (외부거래 ID가 있을 때 사용)")
    @PatchMapping("/{transactionId}/memo")
    public ApplicationResponse<TransactionResponseDTO> updateTransactionMemo(
        @PathVariable Long transactionId,
        @RequestParam Long userId,
        @Valid @RequestBody UpdateTransactionMemoRequestDTO request
    ){
        TransactionVO vo = updateTransactionService.updateTransactionMemo(userId, transactionId, request);
        return ApplicationResponse.onSuccess(TransactionResponseDTO.from(vo));
    }
}
