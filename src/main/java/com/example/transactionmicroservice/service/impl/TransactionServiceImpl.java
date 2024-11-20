package com.example.transactionmicroservice.service.impl;

import com.example.transactionmicroservice.client.BankAccountClient;
import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;
import com.example.transactionmicroservice.repository.TransactionRepository;
import com.example.transactionmicroservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository; // Repository to manage transactions in the database.
    private final BankAccountClient bankAccountClient; // Client to interact with the Bank Account microservice.

    /**
     * Performs a deposit to a specified bank account.
     *
     * @param accountId The ID of the account where the deposit will be made.
     * @param amount    The amount to deposit.
     * @return A Mono containing the created deposit transaction.
     */
    @Override
    public Mono<Transaction> deposit(String accountId, Double amount) {
        // Call the Bank Account microservice to perform the deposit
        return bankAccountClient.deposit(accountId, amount)
                .flatMap(updatedAccount -> {
                    // Create a transaction record for the deposit
                    Transaction transaction = Transaction.builder()
                            .type(TransactionType.DEPOSIT)
                            .amount(amount)
                            .date(LocalDateTime.now())
                            .destinationAccountId(accountId)
                            .build();
                    // Save the transaction in the database
                    return transactionRepository.save(transaction);
                });
    }

    /**
     * Performs a withdrawal from a specified bank account.
     *
     * @param accountId The ID of the account from which the withdrawal will be made.
     * @param amount    The amount to withdraw.
     * @return A Mono containing the created withdrawal transaction.
     */
    @Override
    public Mono<Transaction> withdraw(String accountId, Double amount) {
        // Retrieve the account details from the Bank Account microservice
        return bankAccountClient.getAccount(accountId)
                .flatMap(account -> {
                    // Verify if the account has sufficient balance
                    BigDecimal balance = account.getBalance();
                    BigDecimal amountToWithdraw = BigDecimal.valueOf(amount);
                    if (balance.compareTo(amountToWithdraw) < 0) {
                        // Throw an error if the balance is insufficient
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));
                    }
                    // Call the Bank Account microservice to perform the withdrawal
                    return bankAccountClient.withdraw(accountId, amount);
                })
                .flatMap(updatedAccount -> {
                    // Create a transaction record for the withdrawal
                    Transaction transaction = Transaction.builder()
                            .type(TransactionType.WITHDRAWAL)
                            .amount(amount)
                            .date(LocalDateTime.now())
                            .sourceAccountId(accountId)
                            .build();
                    // Save the transaction in the database
                    return transactionRepository.save(transaction);
                });
    }

    /**
     * Performs a transfer between two bank accounts.
     *
     * @param sourceAccountId      The ID of the account from which the funds will be transferred.
     * @param destinationAccountId The ID of the account to which the funds will be transferred.
     * @param amount               The amount to transfer.
     * @return A Mono containing the created transfer transaction.
     */
    @Override
    public Mono<Transaction> transfer(String sourceAccountId, String destinationAccountId, Double amount) {
        // Retrieve the source account details from the Bank Account microservice
        return bankAccountClient.getAccount(sourceAccountId)
                .flatMap(sourceAccount -> {
                    // Verify if the source account has sufficient balance
                    BigDecimal balance = sourceAccount.getBalance();
                    BigDecimal amountToWithdraw = BigDecimal.valueOf(amount);
                    if (balance.compareTo(amountToWithdraw) < 0) {
                        // Throw an error if the balance is insufficient
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));
                    }
                    // Call the Bank Account microservice to perform the withdrawal from the source account
                    return bankAccountClient.withdraw(sourceAccountId, amount);
                })
                .flatMap(updatedSourceAccount -> {
                    // Call the Bank Account microservice to perform the deposit into the destination account
                    return bankAccountClient.deposit(destinationAccountId, amount);
                })
                .flatMap(updatedDestinationAccount -> {
                    // Create a transaction record for the transfer
                    Transaction transaction = Transaction.builder()
                            .type(TransactionType.TRANSFER)
                            .amount(amount)
                            .date(LocalDateTime.now())
                            .sourceAccountId(sourceAccountId)
                            .destinationAccountId(destinationAccountId)
                            .build();
                    // Save the transaction in the database
                    return transactionRepository.save(transaction);
                });
    }

    /**
     * Retrieves the global transaction history.
     *
     * @return A Flux containing all transactions sorted by date in descending order.
     */
    @Override
    public Flux<Transaction> getGlobalTransactionHistory() {
        // Retrieve all transactions from the database, sorted by date in descending order
        return transactionRepository.findAllByOrderByDateDesc();
    }

    /**
     * Retrieves the transaction history for a specific account.
     *
     * @param accountId The ID of the account whose transaction history is to be retrieved.
     * @return A Flux containing all transactions related to the specified account, sorted by date in descending order.
     */
    @Override
    public Flux<Transaction> getAccountTransactionHistory(String accountId) {
        // Retrieve all transactions related to the specified account from the database
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
    }
}
