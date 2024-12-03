package com.example.transactionmicroservice.service;

import com.example.transactionmicroservice.model.BankAccount;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;

public interface BankAccountService {
    Mono<BankAccount> getAccount(String accountId);
    Mono<BankAccount> deposit(String accountId, Double amount);
    Mono<BankAccount> withdraw(String accountId, Double amount);
    Mono<Pair<BankAccount, BankAccount>> transfer(String sourceAccountId, String destinationAccountId, Double amount);
}
