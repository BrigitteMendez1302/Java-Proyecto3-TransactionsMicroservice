package com.example.transactionmicroservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BankAccount {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;
    private Long customerId;
}
