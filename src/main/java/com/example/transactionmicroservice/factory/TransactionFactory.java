package com.example.transactionmicroservice.factory;

import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;

import java.time.LocalDateTime;

public class TransactionFactory {

    public static Transaction createDepositTransaction(String accountId, Double amount) {
        return Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .date(LocalDateTime.now())
                .destinationAccountId(accountId)
                .build();
    }

    public static Transaction createWithdrawTransaction(String accountId, Double amount) {
        return Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .date(LocalDateTime.now())
                .sourceAccountId(accountId)
                .build();
    }

    public static Transaction createTransferTransaction(String sourceAccountId, String destinationAccountId, Double amount) {
        return Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .date(LocalDateTime.now())
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .build();
    }
}
