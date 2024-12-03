package com.example.transactionmicroservice.mapper;

import com.example.transactionmicroservice.dto.TransactionRequestDto;
import com.example.transactionmicroservice.dto.TransactionResponseDto;
import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;

public class TransactionMapper {

    public static Transaction toTransaction(TransactionRequestDto requestDto) {
        return Transaction.builder()
                .type(TransactionType.valueOf(requestDto.getType().toUpperCase()))
                .amount(requestDto.getAmount())
                .sourceAccountId(requestDto.getSourceAccountId())
                .destinationAccountId(requestDto.getDestinationAccountId())
                .build();
    }

    public static TransactionResponseDto toResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .accountId(transaction.getDestinationAccountId() == null ? transaction.getSourceAccountId() : null)
                .build();
    }
}
