package com.example.transactionmicroservice.service.impl;

import com.example.transactionmicroservice.client.BankAccountClient;
import com.example.transactionmicroservice.model.BankAccount;
import com.example.transactionmicroservice.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountClient bankAccountClient;

    @Override
    public Mono<BankAccount> getAccount(String accountId) {
        validateAccountId(accountId);
        return bankAccountClient.getAccount(accountId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")));
    }

    @Override
    public Mono<BankAccount> deposit(String accountId, Double amount) {
        validateAccountId(accountId);
        validateAmount(amount);
        return bankAccountClient.deposit(accountId, amount)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")));
    }

    public Mono<BankAccount> withdraw(String accountId, Double amount) {
        validateAccountId(accountId);
        validateAmount(amount);
        return getAccount(accountId)
                .flatMap(account -> {
                    BigDecimal balance = account.getBalance();
                    BigDecimal amountToWithdraw = BigDecimal.valueOf(amount);
                    if (balance.compareTo(amountToWithdraw) < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));
                    }
                    return bankAccountClient.withdraw(accountId, amount);
                });
    }

    public Mono<Pair<BankAccount, BankAccount>> transfer(String sourceAccountId, String destinationAccountId, Double amount) {
        validateAccountId(sourceAccountId);
        validateAccountId(destinationAccountId);
        validateAmount(amount);

        if (sourceAccountId.equals(destinationAccountId)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination accounts must be different"));
        }

        return getAccount(sourceAccountId)
                .flatMap(sourceAccount -> {
                    BigDecimal balance = sourceAccount.getBalance();
                    BigDecimal amountToWithdraw = BigDecimal.valueOf(amount);
                    if (balance.compareTo(amountToWithdraw) < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));
                    }
                    return bankAccountClient.withdraw(sourceAccountId, amount)
                            .thenReturn(sourceAccount); // Retorna la cuenta origen actualizada tras el retiro
                })
                .zipWhen(sourceAccount -> bankAccountClient.deposit(destinationAccountId, amount)
                        .map(destinationAccount -> destinationAccount)) // Actualiza y retorna la cuenta destino
                .map(tuple -> Pair.of(tuple.getT1(), tuple.getT2())); // Combina las dos cuentas en un Pair
    }


    // Métodos de validación privados
    private void validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account ID must not be null or blank");
        }
    }

    private void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }
}
