package com.example.transactionmicroservice.controller;

import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.service.TransactionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Performs a deposit to a bank account.
     *
     * @param headers     Request headers.
     * @param requestBody The request body containing the accountId and the deposit amount.
     *                    Example: { "accountId": "1", "amount": 500.0 }
     * @return A Mono containing the created deposit transaction.
     */
    @PostMapping("/transactions/deposit")
    public Mono<Transaction> deposit(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> requestBody) {
        String accountId = (String) requestBody.get("accountId");
        Double amount = (Double) requestBody.get("amount");
        return transactionService.deposit(accountId, amount);
    }

    /**
     * Performs a withdrawal from a bank account.
     *
     * @param headers     Request headers.
     * @param requestBody The request body containing the accountId and the withdrawal amount.
     *                    Example: { "accountId": "1", "amount": 200.0 }
     * @return A Mono containing the created withdrawal transaction.
     */
    @PostMapping("/transactions/withdraw")
    public Mono<Transaction> withdraw(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> requestBody) {
        String accountId = (String) requestBody.get("accountId");
        Double amount = (Double) requestBody.get("amount");
        return transactionService.withdraw(accountId, amount);
    }

    /**
     * Performs a transfer between two bank accounts.
     *
     * @param headers     Request headers.
     * @param requestBody The request body containing the sourceAccountId, destinationAccountId, and the transfer amount.
     *                    Example: { "sourceAccountId": "1", "destinationAccountId": "2", "amount": 300.0 }
     * @return A Mono containing the created transfer transaction.
     */
    @PostMapping("/transactions/transfer")
    public Mono<Transaction> transfer(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> requestBody) {
        String sourceAccountId = (String) requestBody.get("sourceAccountId");
        String destinationAccountId = (String) requestBody.get("destinationAccountId");
        Double amount = (Double) requestBody.get("amount");
        return transactionService.transfer(sourceAccountId, destinationAccountId, amount);
    }

    /**
     * Retrieves the global transaction history.
     *
     * @param headers Request headers.
     * @return A Flux containing all transactions sorted by date.
     */
    @GetMapping("/transactions")
    public Flux<Transaction> getGlobalTransactionHistory(@RequestHeader Map<String, String> headers) {
        return transactionService.getGlobalTransactionHistory();
    }

    /**
     * Retrieves the transaction history for a specific account.
     *
     * @param headers  Request headers.
     * @param accountId The ID of the account.
     * @return A Flux containing transactions related to the specified account.
     */
    @GetMapping("/transactions/account/{accountId}")
    public Flux<Transaction> getAccountTransactionHistory(
            @RequestHeader Map<String, String> headers,
            @PathVariable String accountId) {
        return transactionService.getAccountTransactionHistory(accountId);
    }
}
