//package com.example.transactionmicroservice.controller;
//
//import com.example.transactionmicroservice.model.Transaction;
//import com.example.transactionmicroservice.model.TransactionType;
//import com.example.transactionmicroservice.service.TransactionService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.eq;
//
//@WebFluxTest(TransactionController.class)
//class TransactionControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private TransactionService transactionService;
//
//    @Test
//    void deposit_shouldSucceedWhenValidRequest() {
//        // Mock Transaction
//        Transaction transaction = Transaction.builder()
//                .id("123")
//                .type(TransactionType.DEPOSIT)
//                .amount(500.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId("1")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.deposit(eq("1"), eq(500.0)))
//                .thenReturn(Mono.just(transaction));
//
//        // Request Body
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    @Test
//    void deposit_shouldFailWhenMissingParameters() {
//        // Missing "amount"
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void deposit_shouldFailWhenInvalidAmount() {
//        // Invalid amount (negative)
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", -100.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void deposit_shouldHandleErrorWhenAccountNotFound() {
//        // Mock Service: Account Not Found
//        Mockito.when(transactionService.deposit(eq("1"), eq(500.0)))
//                .thenReturn(Mono.error(new RuntimeException("Account not found")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void deposit_shouldHandleInternalError() {
//        // Mock Service: Internal Error
//        Mockito.when(transactionService.deposit(eq("1"), eq(500.0)))
//                .thenReturn(Mono.error(new RuntimeException("Internal server error")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Test
//    void deposit_shouldFailWhenHeadersMissing() {
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void deposit_shouldHandleBoundaryAmount() {
//        // Mock Transaction for minimum amount
//        Transaction transaction = Transaction.builder()
//                .id("124")
//                .type(TransactionType.DEPOSIT)
//                .amount(0.01)
//                .date(LocalDateTime.now())
//                .destinationAccountId("1")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.deposit(eq("1"), eq(0.01)))
//                .thenReturn(Mono.just(transaction));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 0.01);
//
//        webTestClient.post()
//                .uri("/api/transactions/deposit")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    /**
//     * Performs a withdrawal from a bank account.
//     *
//     * @param headers     Request headers.
//     * @param requestBody The request body containing the accountId and the withdrawal amount.
//     *                    Example: { "accountId": "1", "amount": 200.0 }
//     * @return A Mono containing the created withdrawal transaction.
//     */
//    @PostMapping("/transactions/withdraw")
//    public Mono<Transaction> withdraw(
//            @RequestHeader Map<String, String> headers,
//            @RequestBody Map<String, Object> requestBody) {
//        String accountId = (String) requestBody.get("accountId");
//        Double amount = (Double) requestBody.get("amount");
//        return transactionService.withdraw(accountId, amount);
//    }
//
//    @Test
//    void withdraw_shouldSucceedWhenValidRequest() {
//        // Mock Transaction
//        Transaction transaction = Transaction.builder()
//                .id("321")
//                .type(TransactionType.WITHDRAWAL)
//                .amount(200.0)
//                .date(LocalDateTime.now())
//                .sourceAccountId("1")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.withdraw(eq("1"), eq(200.0)))
//                .thenReturn(Mono.just(transaction));
//
//        // Request Body
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 200.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    @Test
//    void withdraw_shouldFailWhenMissingParameters() {
//        // Missing "amount"
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void withdraw_shouldFailWhenInvalidAmount() {
//        // Invalid amount (negative)
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", -100.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void withdraw_shouldHandleErrorWhenInsufficientBalance() {
//        // Mock Service: Insufficient Balance
//        Mockito.when(transactionService.withdraw(eq("1"), eq(500.0)))
//                .thenReturn(Mono.error(new RuntimeException("Insufficient balance")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void withdraw_shouldHandleErrorWhenAccountNotFound() {
//        // Mock Service: Account Not Found
//        Mockito.when(transactionService.withdraw(eq("1"), eq(200.0)))
//                .thenReturn(Mono.error(new RuntimeException("Account not found")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 200.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void withdraw_shouldHandleInternalError() {
//        // Mock Service: Internal Error
//        Mockito.when(transactionService.withdraw(eq("1"), eq(200.0)))
//                .thenReturn(Mono.error(new RuntimeException("Internal server error")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 200.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Test
//    void withdraw_shouldHandleBoundaryAmount() {
//        // Mock Transaction for minimum amount
//        Transaction transaction = Transaction.builder()
//                .id("322")
//                .type(TransactionType.WITHDRAWAL)
//                .amount(0.01)
//                .date(LocalDateTime.now())
//                .sourceAccountId("1")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.withdraw(eq("1"), eq(0.01)))
//                .thenReturn(Mono.just(transaction));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 0.01);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    @Test
//    void withdraw_shouldFailWhenHeadersMissing() {
//        // Request without headers
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("accountId", "1");
//        requestBody.put("amount", 200.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/withdraw")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void transfer_shouldSucceedWhenValidRequest() {
//        // Mock Transaction
//        Transaction transaction = Transaction.builder()
//                .id("123")
//                .type(TransactionType.TRANSFER)
//                .amount(300.0)
//                .date(LocalDateTime.now())
//                .sourceAccountId("1")
//                .destinationAccountId("2")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.transfer(eq("1"), eq("2"), eq(300.0)))
//                .thenReturn(Mono.just(transaction));
//
//        // Request Body
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", 300.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    @Test
//    void transfer_shouldFailWhenMissingParameters() {
//        // Missing "destinationAccountId"
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("amount", 300.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void transfer_shouldFailWhenInvalidAmount() {
//        // Invalid amount (negative)
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", -100.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void transfer_shouldHandleErrorWhenInsufficientBalance() {
//        // Mock Service: Insufficient Balance
//        Mockito.when(transactionService.transfer(eq("1"), eq("2"), eq(500.0)))
//                .thenReturn(Mono.error(new RuntimeException("Insufficient balance")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", 500.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void transfer_shouldHandleErrorWhenAccountNotFound() {
//        // Mock Service: Source Account Not Found
//        Mockito.when(transactionService.transfer(eq("1"), eq("2"), eq(300.0)))
//                .thenReturn(Mono.error(new RuntimeException("Source account not found")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", 300.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void transfer_shouldFailWhenSourceAndDestinationAreSame() {
//        // Same source and destination account
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "1");
//        requestBody.put("amount", 300.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void transfer_shouldHandleInternalError() {
//        // Mock Service: Internal Error
//        Mockito.when(transactionService.transfer(eq("1"), eq("2"), eq(300.0)))
//                .thenReturn(Mono.error(new RuntimeException("Internal server error")));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", 300.0);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Test
//    void transfer_shouldHandleBoundaryAmount() {
//        // Mock Transaction for minimum amount
//        Transaction transaction = Transaction.builder()
//                .id("124")
//                .type(TransactionType.TRANSFER)
//                .amount(0.01)
//                .date(LocalDateTime.now())
//                .sourceAccountId("1")
//                .destinationAccountId("2")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.transfer(eq("1"), eq("2"), eq(0.01)))
//                .thenReturn(Mono.just(transaction));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("sourceAccountId", "1");
//        requestBody.put("destinationAccountId", "2");
//        requestBody.put("amount", 0.01);
//
//        webTestClient.post()
//                .uri("/api/transactions/transfer")
//                .bodyValue(requestBody)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Transaction.class)
//                .isEqualTo(transaction);
//    }
//
//    @Test
//    void getGlobalTransactionHistory_shouldSucceedWhenTransactionsExist() {
//        // Mock Transactions
//        Transaction transaction1 = Transaction.builder()
//                .id("1")
//                .type(TransactionType.DEPOSIT)
//                .amount(100.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId("1")
//                .build();
//
//        Transaction transaction2 = Transaction.builder()
//                .id("2")
//                .type(TransactionType.WITHDRAWAL)
//                .amount(50.0)
//                .date(LocalDateTime.now().minusDays(1))
//                .sourceAccountId("2")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.getGlobalTransactionHistory())
//                .thenReturn(Flux.just(transaction1, transaction2));
//
//        webTestClient.get()
//                .uri("/api/transactions")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(2)
//                .contains(transaction1, transaction2);
//    }
//
//    @Test
//    void getGlobalTransactionHistory_shouldReturnEmptyWhenNoTransactionsExist() {
//        // Mock Service: No Transactions
//        Mockito.when(transactionService.getGlobalTransactionHistory())
//                .thenReturn(Flux.empty());
//
//        webTestClient.get()
//                .uri("/api/transactions")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(0);
//    }
//
//    @Test
//    void getGlobalTransactionHistory_shouldHandleErrorWhenServiceFails() {
//        // Mock Service: Internal Error
//        Mockito.when(transactionService.getGlobalTransactionHistory())
//                .thenReturn(Flux.error(new RuntimeException("Internal server error")));
//
//        webTestClient.get()
//                .uri("/api/transactions")
//                .exchange()
//                .expectStatus().is5xxServerError();
//    }
//
//    @Test
//    void getGlobalTransactionHistory_shouldFailWhenHeadersMissing() {
//        // Simulate missing headers
//        webTestClient.get()
//                .uri("/api/transactions")
//                .header("Authorization", "")
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void getGlobalTransactionHistory_shouldHandleLargeDataSets() {
//        // Mock large list of transactions
//        Transaction transaction = Transaction.builder()
//                .id("1")
//                .type(TransactionType.DEPOSIT)
//                .amount(100.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId("1")
//                .build();
//
//        Mockito.when(transactionService.getGlobalTransactionHistory())
//                .thenReturn(Flux.fromIterable(Arrays.asList(transaction, transaction, transaction)));
//
//        webTestClient.get()
//                .uri("/api/transactions")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(3);
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldSucceedWhenTransactionsExist() {
//        // Mock Transactions
//        Transaction transaction1 = Transaction.builder()
//                .id("1")
//                .type(TransactionType.DEPOSIT)
//                .amount(200.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId("account-1")
//                .build();
//
//        Transaction transaction2 = Transaction.builder()
//                .id("2")
//                .type(TransactionType.WITHDRAWAL)
//                .amount(100.0)
//                .date(LocalDateTime.now().minusDays(1))
//                .sourceAccountId("account-1")
//                .build();
//
//        // Mock Service
//        Mockito.when(transactionService.getAccountTransactionHistory("account-1"))
//                .thenReturn(Flux.just(transaction1, transaction2));
//
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(2)
//                .contains(transaction1, transaction2);
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldReturnEmptyWhenNoTransactionsExist() {
//        // Mock Service: No Transactions
//        Mockito.when(transactionService.getAccountTransactionHistory("account-1"))
//                .thenReturn(Flux.empty());
//
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(0);
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldHandleErrorWhenAccountNotFound() {
//        // Mock Service: Account Not Found
//        Mockito.when(transactionService.getAccountTransactionHistory("account-1"))
//                .thenReturn(Flux.error(new RuntimeException("Account not found")));
//
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldHandleInternalError() {
//        // Mock Service: Internal Error
//        Mockito.when(transactionService.getAccountTransactionHistory("account-1"))
//                .thenReturn(Flux.error(new RuntimeException("Internal server error")));
//
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .exchange()
//                .expectStatus().is5xxServerError();
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldFailWhenHeadersMissing() {
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .header("Authorization", "")
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldHandleLargeDataSets() {
//        // Mock large list of transactions
//        Transaction transaction = Transaction.builder()
//                .id("1")
//                .type(TransactionType.DEPOSIT)
//                .amount(100.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId("account-1")
//                .build();
//
//        Mockito.when(transactionService.getAccountTransactionHistory("account-1"))
//                .thenReturn(Flux.fromIterable(Arrays.asList(transaction, transaction, transaction)));
//
//        webTestClient.get()
//                .uri("/api/transactions/account/account-1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Transaction.class)
//                .hasSize(3);
//    }
//
//    @Test
//    void getAccountTransactionHistory_shouldFailWhenAccountIdIsInvalid() {
//        webTestClient.get()
//                .uri("/api/transactions/account/invalid-id")
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//}
