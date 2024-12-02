package com.example.transactionmicroservice.service.impl;

import com.example.transactionmicroservice.client.BankAccountClient;
import com.example.transactionmicroservice.model.AccountType;
import com.example.transactionmicroservice.model.BankAccount;
import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;
import com.example.transactionmicroservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountClient bankAccountClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deposit_shouldCreateTransaction() {
        String accountId = "123";
        Double amount = 100.0;

        Transaction transaction = Transaction.builder()
                .id("txn123")
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .date(LocalDateTime.now())
                .destinationAccountId(accountId)
                .build();

        when(bankAccountClient.deposit(accountId, amount)).thenReturn(Mono.just(
                BankAccount.builder()
                        .id(1L)
                        .accountNumber(accountId)
                        .balance(BigDecimal.valueOf(200))
                        .accountType(null)
                        .customerId(10L)
                        .build()
        ));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Transaction> result = transactionService.deposit(accountId, amount);

        StepVerifier.create(result)
                .expectNextMatches(savedTransaction ->
                        savedTransaction.getType() == TransactionType.DEPOSIT
                                && savedTransaction.getAmount().equals(amount)
                                && savedTransaction.getDestinationAccountId().equals(accountId))
                .verifyComplete();

        verify(bankAccountClient, times(1)).deposit(accountId, amount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldCreateTransactionAndUpdateBalance() {
        String accountId = "123";
        Double depositAmount = 50.0;

        // Mock the initial state of the account
        BankAccount initialAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(null)
                .customerId(10L)
                .build();

        // Mock the updated account after deposit
        BankAccount updatedAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(350)) // Balance increased by 50
                .accountType(null)
                .customerId(10L)
                .build();

        // Mocking behavior
        when(bankAccountClient.deposit(accountId, depositAmount)).thenReturn(Mono.just(updatedAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Execute the method
        Mono<Transaction> result = transactionService.deposit(accountId, depositAmount);

        // Verify the results
        StepVerifier.create(result)
                .expectNextMatches(transaction ->
                        transaction.getType() == TransactionType.DEPOSIT &&
                                transaction.getAmount().equals(depositAmount) &&
                                transaction.getDestinationAccountId().equals(accountId))
                .verifyComplete();

        // Verify that the deposit call was made with the correct parameters
        verify(bankAccountClient, times(1)).deposit(accountId, depositAmount);

        // Verify that the transaction was saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldHandleErrorFromBankAccountService() {
        String accountId = "123";
        Double depositAmount = 50.0;

        // Simulate an error in the BankAccountClient
        when(bankAccountClient.deposit(accountId, depositAmount))
                .thenReturn(Mono.error(new RuntimeException("Bank service unavailable")));

        // Execute the method
        Mono<Transaction> result = transactionService.deposit(accountId, depositAmount);

        // Verify that an error is propagated
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Bank service unavailable"))
                .verify();

        // Verify no transaction is saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldHandleErrorWhenSavingTransaction() {
        String accountId = "123";
        Double depositAmount = 50.0;

        BankAccount updatedAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(350))
                .accountType(null)
                .customerId(10L)
                .build();

        // Mock successful deposit but failure in saving the transaction
        when(bankAccountClient.deposit(accountId, depositAmount)).thenReturn(Mono.just(updatedAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Database save error")));

        // Execute the method
        Mono<Transaction> result = transactionService.deposit(accountId, depositAmount);

        // Verify that an error is propagated
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database save error"))
                .verify();

        // Verify the deposit call was made
        verify(bankAccountClient, times(1)).deposit(accountId, depositAmount);

        // Verify the transaction save attempt was made
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldCreateWithdrawalTransaction() {
        String accountId = "123";
        Double amount = 50.0;

        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(250))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));
        when(bankAccountClient.withdraw(accountId, amount)).thenReturn(Mono.just(updatedAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectNextMatches(transaction ->
                        transaction.getType() == TransactionType.WITHDRAWAL &&
                                transaction.getAmount().equals(amount) &&
                                transaction.getSourceAccountId().equals(accountId))
                .verifyComplete();

        verify(bankAccountClient, times(1)).withdraw(accountId, amount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldRejectInsufficientBalanceForSavings() {
        String accountId = "123";
        Double amount = 350.0;

        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                        throwable.getMessage().contains("Insufficient balance"))
                .verify();

        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

//    @Test
//    void withdraw_shouldAllowOverdraftForChecking() {
//        String accountId = "123";
//        Double amount = 350.0;
//
//        // Configurar un objeto válido de BankAccount para el mock
//        BankAccount account = BankAccount.builder()
//                .id(1L)
//                .accountNumber(accountId)
//                .balance(BigDecimal.valueOf(200)) // Suficiente para sobregiro permitido
//                .accountType(AccountType.CHECKING)
//                .customerId(10L)
//                .build();
//
//        BankAccount updatedAccount = BankAccount.builder()
//                .id(1L)
//                .accountNumber(accountId)
//                .balance(BigDecimal.valueOf(-150)) // Sobregiro permitido
//                .accountType(AccountType.CHECKING)
//                .customerId(10L)
//                .build();
//
//        // Configurar los mocks
//        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));
//        when(bankAccountClient.withdraw(accountId, amount)).thenReturn(Mono.just(updatedAccount));
//        when(transactionRepository.save(any(Transaction.class)))
//                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
//
//        // Ejecutar el método withdraw
//        Mono<Transaction> result = transactionService.withdraw(accountId, amount);
//
//        // Verificar el resultado
//        StepVerifier.create(result)
//                .expectNextMatches(transaction ->
//                        transaction.getType() == TransactionType.WITHDRAWAL &&
//                                transaction.getAmount().equals(amount) &&
//                                transaction.getSourceAccountId().equals(accountId))
//                .verifyComplete();
//
//        // Verificar interacciones
//        verify(bankAccountClient, times(1)).getAccount(accountId);
//        verify(bankAccountClient, times(1)).withdraw(accountId, amount);
//        verify(transactionRepository, times(1)).save(any(Transaction.class));
//    }


    @Test
    void withdraw_shouldRejectExcessiveOverdraftForChecking() {
        String accountId = "123";
        Double amount = 800.0;

        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(200))
                .accountType(AccountType.CHECKING)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                        throwable.getMessage().contains("Insufficient balance"))
                .verify();

        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldHandleErrorGettingAccountDetails() {
        String accountId = "123";
        Double amount = 50.0;

        when(bankAccountClient.getAccount(accountId))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Service unavailable"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(accountId);
        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldHandleErrorDuringWithdrawal() {
        String accountId = "123";
        Double amount = 50.0;

        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));
        when(bankAccountClient.withdraw(accountId, amount))
                .thenReturn(Mono.error(new RuntimeException("Withdrawal failed")));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Withdrawal failed"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(accountId);
        verify(bankAccountClient, times(1)).withdraw(accountId, amount);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldHandleErrorSavingTransaction() {
        String accountId = "123";
        Double amount = 50.0;

        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(accountId)
                .balance(BigDecimal.valueOf(250))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(accountId)).thenReturn(Mono.just(account));
        when(bankAccountClient.withdraw(accountId, amount)).thenReturn(Mono.just(updatedAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Transaction> result = transactionService.withdraw(accountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(bankAccountClient, times(1)).withdraw(accountId, amount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldCreateTransferTransaction() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 100.0;

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedSourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(200))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedDestinationAccount = BankAccount.builder()
                .id(2L)
                .accountNumber(destinationAccountId)
                .balance(BigDecimal.valueOf(400))
                .accountType(AccountType.SAVINGS)
                .customerId(11L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));
        when(bankAccountClient.withdraw(sourceAccountId, amount)).thenReturn(Mono.just(updatedSourceAccount));
        when(bankAccountClient.deposit(destinationAccountId, amount)).thenReturn(Mono.just(updatedDestinationAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectNextMatches(transaction ->
                        transaction.getType() == TransactionType.TRANSFER &&
                                transaction.getAmount().equals(amount) &&
                                transaction.getSourceAccountId().equals(sourceAccountId) &&
                                transaction.getDestinationAccountId().equals(destinationAccountId))
                .verifyComplete();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, times(1)).withdraw(sourceAccountId, amount);
        verify(bankAccountClient, times(1)).deposit(destinationAccountId, amount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldRejectInsufficientBalanceForSavings() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 500.0;

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                        throwable.getMessage().contains("Insufficient balance"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(bankAccountClient, never()).deposit(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldRejectExcessiveOverdraftForChecking() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 900.0; // Exceeds overdraft limit

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.CHECKING)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                        throwable.getMessage().contains("Insufficient balance"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(bankAccountClient, never()).deposit(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldHandleErrorGettingSourceAccountDetails() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 100.0;

        when(bankAccountClient.getAccount(sourceAccountId))
                .thenReturn(Mono.error(new RuntimeException("Source account service unavailable")));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Source account service unavailable"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, never()).withdraw(anyString(), anyDouble());
        verify(bankAccountClient, never()).deposit(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldHandleErrorDuringSourceAccountWithdrawal() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 100.0;

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));
        when(bankAccountClient.withdraw(sourceAccountId, amount))
                .thenReturn(Mono.error(new RuntimeException("Withdrawal failed")));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Withdrawal failed"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, times(1)).withdraw(sourceAccountId, amount);
        verify(bankAccountClient, never()).deposit(anyString(), anyDouble());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldHandleErrorDuringDestinationAccountDeposit() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 100.0;

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedSourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(200))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));
        when(bankAccountClient.withdraw(sourceAccountId, amount)).thenReturn(Mono.just(updatedSourceAccount));
        when(bankAccountClient.deposit(destinationAccountId, amount))
                .thenReturn(Mono.error(new RuntimeException("Deposit failed")));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Deposit failed"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, times(1)).withdraw(sourceAccountId, amount);
        verify(bankAccountClient, times(1)).deposit(destinationAccountId, amount);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldHandleErrorSavingTransaction() {
        String sourceAccountId = "123";
        String destinationAccountId = "456";
        Double amount = 100.0;

        BankAccount sourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(300))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedSourceAccount = BankAccount.builder()
                .id(1L)
                .accountNumber(sourceAccountId)
                .balance(BigDecimal.valueOf(200))
                .accountType(AccountType.SAVINGS)
                .customerId(10L)
                .build();

        BankAccount updatedDestinationAccount = BankAccount.builder()
                .id(2L)
                .accountNumber(destinationAccountId)
                .balance(BigDecimal.valueOf(400))
                .accountType(AccountType.SAVINGS)
                .customerId(11L)
                .build();

        when(bankAccountClient.getAccount(sourceAccountId)).thenReturn(Mono.just(sourceAccount));
        when(bankAccountClient.withdraw(sourceAccountId, amount)).thenReturn(Mono.just(updatedSourceAccount));
        when(bankAccountClient.deposit(destinationAccountId, amount)).thenReturn(Mono.just(updatedDestinationAccount));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Transaction> result = transactionService.transfer(sourceAccountId, destinationAccountId, amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(bankAccountClient, times(1)).getAccount(sourceAccountId);
        verify(bankAccountClient, times(1)).withdraw(sourceAccountId, amount);
        verify(bankAccountClient, times(1)).deposit(destinationAccountId, amount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getGlobalTransactionHistory_shouldReturnEmptyFlux() {
        when(transactionRepository.findAllByOrderByDateDesc()).thenReturn(Flux.empty());

        Flux<Transaction> result = transactionService.getGlobalTransactionHistory();

        StepVerifier.create(result)
                .expectNextCount(0) // No transactions should be emitted
                .verifyComplete();

        verify(transactionRepository, times(1)).findAllByOrderByDateDesc();
    }

    @Test
    void getGlobalTransactionHistory_shouldReturnTransactionList() {
        Transaction transaction1 = Transaction.builder()
                .id("txn1")
                .type(TransactionType.DEPOSIT)
                .amount(100.0)
                .date(LocalDateTime.now())
                .destinationAccountId("123")
                .build();

        Transaction transaction2 = Transaction.builder()
                .id("txn2")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now().minusDays(1))
                .sourceAccountId("123")
                .build();

        when(transactionRepository.findAllByOrderByDateDesc()).thenReturn(Flux.just(transaction1, transaction2));

        Flux<Transaction> result = transactionService.getGlobalTransactionHistory();

        StepVerifier.create(result)
                .expectNext(transaction1)
                .expectNext(transaction2)
                .verifyComplete();

        verify(transactionRepository, times(1)).findAllByOrderByDateDesc();
    }

    @Test
    void getGlobalTransactionHistory_shouldReturnTransactionsInDescendingOrder() {
        Transaction transaction1 = Transaction.builder()
                .id("txn1")
                .type(TransactionType.DEPOSIT)
                .amount(100.0)
                .date(LocalDateTime.now())
                .destinationAccountId("123")
                .build();

        Transaction transaction2 = Transaction.builder()
                .id("txn2")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now().minusDays(1))
                .sourceAccountId("123")
                .build();

        when(transactionRepository.findAllByOrderByDateDesc()).thenReturn(Flux.just(transaction1, transaction2));

        Flux<Transaction> result = transactionService.getGlobalTransactionHistory();

        StepVerifier.create(result)
                .expectNextMatches(transaction -> transaction.getDate().isAfter(transaction2.getDate()))
                .expectNextMatches(transaction -> transaction.getDate().isBefore(transaction1.getDate()))
                .verifyComplete();

        verify(transactionRepository, times(1)).findAllByOrderByDateDesc();
    }

    @Test
    void getGlobalTransactionHistory_shouldPropagateError() {
        when(transactionRepository.findAllByOrderByDateDesc())
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        Flux<Transaction> result = transactionService.getGlobalTransactionHistory();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(transactionRepository, times(1)).findAllByOrderByDateDesc();
    }

    @Test
    void getAccountTransactionHistory_shouldReturnEmptyFlux() {
        String accountId = "123";

        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId))
                .thenReturn(Flux.empty());

        Flux<Transaction> result = transactionService.getAccountTransactionHistory(accountId);

        StepVerifier.create(result)
                .expectNextCount(0) // No transactions should be emitted
                .verifyComplete();

        verify(transactionRepository, times(1))
                .findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
    }

    @Test
    void getAccountTransactionHistory_shouldReturnTransactionList() {
        String accountId = "123";

        Transaction transaction1 = Transaction.builder()
                .id("txn1")
                .type(TransactionType.DEPOSIT)
                .amount(100.0)
                .date(LocalDateTime.now())
                .destinationAccountId(accountId)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id("txn2")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now().minusDays(1))
                .sourceAccountId(accountId)
                .build();

        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId))
                .thenReturn(Flux.just(transaction1, transaction2));

        Flux<Transaction> result = transactionService.getAccountTransactionHistory(accountId);

        StepVerifier.create(result)
                .expectNext(transaction1)
                .expectNext(transaction2)
                .verifyComplete();

        verify(transactionRepository, times(1))
                .findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
    }

//    @Test
//    void getAccountTransactionHistory_shouldReturnTransactionsForAccount() {
//        String accountId = "123";
//
//        Transaction transaction1 = Transaction.builder()
//                .id("txn1")
//                .type(TransactionType.DEPOSIT)
//                .amount(100.0)
//                .date(LocalDateTime.now())
//                .destinationAccountId(accountId)
//                .build();
//
//        Transaction transaction2 = Transaction.builder()
//                .id("txn2")
//                .type(TransactionType.WITHDRAWAL)
//                .amount(50.0)
//                .date(LocalDateTime.now().minusDays(1))
//                .sourceAccountId(accountId)
//                .build();
//
//        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId))
//                .thenReturn(Flux.just(transaction1, transaction2));
//
//        Flux<Transaction> result = transactionService.getAccountTransactionHistory(accountId);
//
//        StepVerifier.create(result)
//                .expectNextMatches(transaction -> transaction.getDestinationAccountId().equals(accountId) ||
//                        transaction.getSourceAccountId().equals(accountId))
//                .expectNextMatches(transaction -> transaction.getDestinationAccountId().equals(accountId) ||
//                        transaction.getSourceAccountId().equals(accountId))
//                .verifyComplete();
//
//        verify(transactionRepository, times(1))
//                .findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
//    }

    @Test
    void getAccountTransactionHistory_shouldReturnTransactionsInDescendingOrder() {
        String accountId = "123";

        Transaction transaction1 = Transaction.builder()
                .id("txn1")
                .type(TransactionType.DEPOSIT)
                .amount(100.0)
                .date(LocalDateTime.now())
                .destinationAccountId(accountId)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id("txn2")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now().minusDays(1))
                .sourceAccountId(accountId)
                .build();

        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId))
                .thenReturn(Flux.just(transaction1, transaction2));

        Flux<Transaction> result = transactionService.getAccountTransactionHistory(accountId);

        StepVerifier.create(result)
                .expectNextMatches(transaction -> transaction.getDate().isAfter(transaction2.getDate()))
                .expectNextMatches(transaction -> transaction.getDate().isBefore(transaction1.getDate()))
                .verifyComplete();

        verify(transactionRepository, times(1))
                .findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
    }

    @Test
    void getAccountTransactionHistory_shouldPropagateError() {
        String accountId = "123";

        when(transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId))
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        Flux<Transaction> result = transactionService.getAccountTransactionHistory(accountId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(transactionRepository, times(1))
                .findBySourceAccountIdOrDestinationAccountIdOrderByDateDesc(accountId, accountId);
    }

}
