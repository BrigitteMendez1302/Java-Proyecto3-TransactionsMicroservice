package com.example.transactionmicroservice.repository;

import com.example.transactionmicroservice.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repository interface for managing Transaction entities in MongoDB.
 * Extends ReactiveMongoRepository to provide reactive CRUD operations.
 */
@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    /**
     * Retrieves the global transaction history.
     * All transactions are returned and sorted by date in descending order.
     *
     * @return A Flux containing all transactions, sorted by date in descending order.
     */
    Flux<Transaction> findAllByOrderByDateDesc();

    /**
     * Retrieves the transaction history for a specific account.
     * Transactions are filtered where the specified account is either the source or the destination.
     * Results are sorted by date in descending order.
     *
     * @param sourceAccountId      The ID of the source account.
     * @param destinationAccountId The ID of the destination account.
     * @return A Flux containing transactions related to the specified account, sorted by date in descending order.
     */
    Flux<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(String sourceAccountId, String destinationAccountId);

    /**
     * Retrieves transactions by their type.
     * Transactions are filtered by the specified type and sorted by date in descending order.
     *
     * @param type The type of transactions to retrieve (e.g., DEPOSIT, WITHDRAWAL, TRANSFER).
     * @return A Flux containing transactions of the specified type, sorted by date in descending order.
     */
    Flux<Transaction> findByTypeOrderByDateDesc(String type);
}
