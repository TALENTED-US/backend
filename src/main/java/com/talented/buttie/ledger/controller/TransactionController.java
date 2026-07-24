package com.talented.buttie.ledger.controller;

import com.talented.buttie.ledger.dto.response.TransactionResponseDTO;
import com.talented.buttie.ledger.service.GetTransactionService;
import io.swagger.annotations.Api;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Transaction")
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    public GetTransactionService getTransactionService;

    @GetMapping("")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions(@RequestParam Long userId){
        List<TransactionResponseDTO> response = getTransactionService.getAllTransactions(userId);
        return  ResponseEntity.ok(response);
    }
}
