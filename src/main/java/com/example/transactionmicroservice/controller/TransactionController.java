package com.example.transactionmicroservice.controller;

import com.example.transactionmicroservice.dto.TransactionRequestDto;
import com.example.transactionmicroservice.dto.TransactionResponseDto;
import com.example.transactionmicroservice.mapper.TransactionMapper;
import com.example.transactionmicroservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions/deposit")
    public Mono<TransactionResponseDto> deposit(@RequestBody @Valid TransactionRequestDto request) {
        return transactionService.deposit(request.getAccountId(), request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @PostMapping("/transactions/withdraw")
    public Mono<TransactionResponseDto> withdraw(@RequestBody @Valid TransactionRequestDto request) {
        return transactionService.withdraw(request.getAccountId(), request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @PostMapping("/transactions/transfer")
    public Mono<TransactionResponseDto> transfer(@RequestBody @Valid TransactionRequestDto request) {
        return transactionService.transfer(request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @GetMapping("/transactions")
    public Flux<TransactionResponseDto> getGlobalTransactionHistory() {
        return transactionService.getGlobalTransactionHistory()
                .map(TransactionMapper::toResponseDto);
    }

    @GetMapping("/transactions/account/{accountId}")
    public Flux<TransactionResponseDto> getAccountTransactionHistory(@PathVariable String accountId) {
        return transactionService.getAccountTransactionHistory(accountId)
                .map(TransactionMapper::toResponseDto);
    }
}
