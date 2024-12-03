package com.example.transactionmicroservice.controller;

import com.example.transactionmicroservice.dto.TransactionRequestDto;
import com.example.transactionmicroservice.dto.TransactionResponseDto;
import com.example.transactionmicroservice.mapper.TransactionMapper;
import com.example.transactionmicroservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Tag(name = "Transaction", description = "Operaciones sobre transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit money", description = "Deposits a specified amount into a bank account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping("/transactions/deposit")
    public Mono<TransactionResponseDto> deposit(
            @RequestBody @Valid @Parameter(description = "Transaction details for deposit", required = true) TransactionRequestDto request) {
        return transactionService.deposit(request.getAccountId(), request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @Operation(summary = "Withdraw money", description = "Withdraws a specified amount from a bank account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping("/transactions/withdraw")
    public Mono<TransactionResponseDto> withdraw(
            @RequestBody @Valid @Parameter(description = "Transaction details for withdrawal", required = true) TransactionRequestDto request) {
        return transactionService.withdraw(request.getAccountId(), request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @Operation(summary = "Transfer money", description = "Transfers a specified amount from one bank account to another.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping("/transactions/transfer")
    public Mono<TransactionResponseDto> transfer(
            @RequestBody @Valid @Parameter(description = "Transaction details for transfer", required = true) TransactionRequestDto request) {
        return transactionService.transfer(request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        request.getAmount())
                .map(TransactionMapper::toResponseDto);
    }

    @Operation(summary = "Get global transaction history", description = "Retrieves the global history of all transactions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No transactions found", content = @Content)
    })
    @GetMapping("/transactions")
    public Flux<TransactionResponseDto> getGlobalTransactionHistory() {
        return transactionService.getGlobalTransactionHistory()
                .map(TransactionMapper::toResponseDto);
    }

    @Operation(summary = "Get transaction history for an account", description = "Retrieves the transaction history for a specific account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No transactions found for the account", content = @Content)
    })
    @GetMapping("/transactions/account/{accountId}")
    public Flux<TransactionResponseDto> getAccountTransactionHistory(
            @PathVariable @Parameter(description = "ID of the account", required = true) String accountId) {
        return transactionService.getAccountTransactionHistory(accountId)
                .map(TransactionMapper::toResponseDto);
    }
}
