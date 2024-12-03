package com.example.transactionmicroservice.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TransactionRequestDto {
    @NotBlank(message = "Transaction type is required")
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER

    private String sourceAccountId; // Required for TRANSFER

    private String destinationAccountId; // Required for TRANSFER

    @NotBlank(message = "Account ID is required for deposits and withdrawals")
    private String accountId; // Required for DEPOSIT and WITHDRAWAL

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount; // Required for all transaction types
}
