package com.example.transactionmicroservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponseDto {
    private String id; // Transaction ID
    private String type; // Transaction type: DEPOSIT, WITHDRAWAL, TRANSFER
    private Double amount; // Transaction amount
    private LocalDateTime date; // Transaction date and time
    private String sourceAccountId; // Source account ID
    private String destinationAccountId; // Destination account ID
    private String accountId; // Account ID for deposits or withdrawals
}
