package com.example.transactionmicroservice.service;

import com.example.transactionmicroservice.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing transactions.
 * Defines operations for deposits, withdrawals, transfers, and retrieving transaction histories.
 */
public interface TransactionService {

    /**
     * Performs a deposit to a specified bank account.
     *
     * @param accountId The ID of the account where the deposit will be made.
     * @param amount    The amount to be deposited.
     * @return A Mono containing the saved deposit transaction.
     */
    Mono<Transaction> deposit(String accountId, Double amount);

    /**
     * Performs a withdrawal from a specified bank account.
     *
     * @param accountId The ID of the account from which the withdrawal will be made.
     * @param amount    The amount to be withdrawn.
     * @return A Mono containing the saved withdrawal transaction.
     */
    Mono<Transaction> withdraw(String accountId, Double amount);

    /**
     * Performs a transfer of funds between two bank accounts.
     *
     * @param sourceAccountId      The ID of the account from which the funds will be transferred.
     * @param destinationAccountId The ID of the account to which the funds will be transferred.
     * @param amount               The amount to be transferred.
     * @return A Mono containing the saved transfer transaction.
     */
    Mono<Transaction> transfer(String sourceAccountId, String destinationAccountId, Double amount);

    /**
     * Retrieves the global transaction history.
     * Includes all transactions in the system, sorted by date.
     *
     * @return A Flux containing all transactions, sorted by date in descending order.
     */
    Flux<Transaction> getGlobalTransactionHistory();

    /**
     * Retrieves the transaction history for a specific account.
     * Includes transactions where the account is either the source or the destination.
     *
     * @param accountId The ID of the account whose transaction history is to be retrieved.
     * @return A Flux containing all transactions related to the specified account, sorted by date in descending order.
     */
    Flux<Transaction> getAccountTransactionHistory(String accountId);
}
