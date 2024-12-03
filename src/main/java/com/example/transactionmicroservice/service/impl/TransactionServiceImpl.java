package com.example.transactionmicroservice.service.impl;

import com.example.transactionmicroservice.client.BankAccountClient;
import com.example.transactionmicroservice.factory.TransactionFactory;
import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;
import com.example.transactionmicroservice.repository.TransactionRepository;
import com.example.transactionmicroservice.service.BankAccountService;
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
    private final BankAccountService bankAccountService; // Inyectado automáticamente por Spring

    /**
     * Performs a deposit to a specified bank account.
     *
     * @param accountId The ID of the account where the deposit will be made.
     * @param amount    The amount to deposit.
     * @return A Mono containing the created deposit transaction.
     */
    @Override
    public Mono<Transaction> deposit(String accountId, Double amount) {
        // Use BankAccountService to perform the deposit with validations
        return bankAccountService.deposit(accountId, amount)
                .flatMap(updatedAccount -> {
                    // Use TransactionFactory to create the transaction
                    Transaction transaction = TransactionFactory.createDepositTransaction(accountId, amount);
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
        // Use BankAccountService to handle the withdrawal with validations
        return bankAccountService.withdraw(accountId, amount)
                .flatMap(updatedAccount -> {
                    // Use TransactionFactory to create the withdrawal transaction
                    Transaction transaction = TransactionFactory.createWithdrawTransaction(accountId, amount);
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
        return bankAccountService.transfer(sourceAccountId, destinationAccountId, amount)
                .flatMap(accounts -> {
                    Transaction transaction = TransactionFactory.createTransferTransaction(
                            sourceAccountId,          // The account from which the funds are transferred
                            destinationAccountId,     // The account to which the funds are deposited
                            amount                    // The amount being transferred
                    );

                    // Save the transaction record in the database using the TransactionRepository
                    // This ensures that the transfer details are persisted for future reference.
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
