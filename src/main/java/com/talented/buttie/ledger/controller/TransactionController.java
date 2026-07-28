package com.talented.buttie.ledger.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.response.TransactionResponseDTO;
import com.talented.buttie.ledger.service.GetTransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Api(tags = "거래 목록 관리")
public class TransactionController {
    private final GetTransactionService getTransactionService;

    @ApiOperation("거래 목록 조회")
    @GetMapping("")
    public ApplicationResponse<List<TransactionResponseDTO>> getAllTransactions(
        @ApiParam(value= "사용자 ID", required = true)
        @RequestParam Long userId
    ){
        List<TransactionVO> transactions = getTransactionService.getAllTransactions(userId);


        return ApplicationResponse.onSuccess(TransactionResponseDTO.fromList(transactions));

    }


}
