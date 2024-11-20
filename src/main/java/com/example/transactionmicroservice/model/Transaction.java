package com.example.transactionmicroservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@Document(collection = "transactions")
public class Transaction {

    @BsonId
    private String id; // Unique identifier for the transaction
    private TransactionType type; // Type of transaction: DEPOSIT, WITHDRAWAL, TRANSFER
    private Double amount; // Amount involved in the transaction
    private LocalDateTime date; // Date and time of the transaction
    private String sourceAccountId; // Originating account ID (optional for DEPOSIT)
    private String destinationAccountId; // Destination account ID (only for TRANSFER)
}
