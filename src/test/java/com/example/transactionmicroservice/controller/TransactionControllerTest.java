package com.example.transactionmicroservice.controller;

import com.example.transactionmicroservice.dto.TransactionRequestDto;
import com.example.transactionmicroservice.dto.TransactionResponseDto;
import com.example.transactionmicroservice.model.Transaction;
import com.example.transactionmicroservice.model.TransactionType;
import com.example.transactionmicroservice.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void deposit_shouldHandleErrorFromService() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountId("123");
        request.setAmount(100.0);

        when(transactionService.deposit("123", 100.0))
                .thenReturn(Mono.error(new RuntimeException("Deposit service failed")));

        // Act
        Mono<TransactionResponseDto> result = transactionController.deposit(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Deposit service failed"))
                .verify();

        verify(transactionService, times(1)).deposit("123", 100.0);
    }

    @Test
    void withdraw_shouldProcessSuccessfulWithdrawal() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountId("123");
        request.setAmount(50.0);

        Transaction transaction = Transaction.builder()
                .id("txn456")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now())
                .sourceAccountId("123")
                .build();

        when(transactionService.withdraw("123", 50.0)).thenReturn(Mono.just(transaction));

        // Act
        Mono<TransactionResponseDto> result = transactionController.withdraw(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getId().equals("txn456") &&
                                response.getType().equals("WITHDRAWAL") &&
                                response.getAmount().equals(50.0) &&
                                response.getAccountId().equals("123"))
                .verifyComplete();

        verify(transactionService, times(1)).withdraw("123", 50.0);
    }

    @Test
    void withdraw_shouldHandleErrorFromService() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountId("123");
        request.setAmount(50.0);

        when(transactionService.withdraw("123", 50.0))
                .thenReturn(Mono.error(new RuntimeException("Withdrawal service failed")));

        // Act
        Mono<TransactionResponseDto> result = transactionController.withdraw(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Withdrawal service failed"))
                .verify();

        verify(transactionService, times(1)).withdraw("123", 50.0);
    }

    @Test
    void withdraw_shouldMapResponseCorrectly() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountId("123");
        request.setAmount(50.0);

        Transaction transaction = Transaction.builder()
                .id("txn456")
                .type(TransactionType.WITHDRAWAL)
                .amount(50.0)
                .date(LocalDateTime.now())
                .sourceAccountId("123")
                .build();

        when(transactionService.withdraw("123", 50.0)).thenReturn(Mono.just(transaction));

        // Act
        Mono<TransactionResponseDto> result = transactionController.withdraw(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getId().equals("txn456") &&
                                response.getType().equals("WITHDRAWAL") &&
                                response.getAmount().equals(50.0) &&
                                response.getDate() != null &&
                                response.getAccountId().equals("123"))
                .verifyComplete();

        verify(transactionService, times(1)).withdraw("123", 50.0);
    }

    @Test
    void transfer_shouldProcessSuccessfulTransfer() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setSourceAccountId("123");
        request.setDestinationAccountId("456");
        request.setAmount(200.0);

        Transaction transaction = Transaction.builder()
                .id("txn789")
                .type(TransactionType.TRANSFER)
                .amount(200.0)
                .date(LocalDateTime.now())
                .sourceAccountId("123")
                .destinationAccountId("456")
                .build();

        when(transactionService.transfer("123", "456", 200.0)).thenReturn(Mono.just(transaction));

        // Act
        Mono<TransactionResponseDto> result = transactionController.transfer(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getId().equals("txn789") &&
                                response.getType().equals("TRANSFER") &&
                                response.getAmount().equals(200.0) &&
                                response.getSourceAccountId().equals("123") &&
                                response.getDestinationAccountId().equals("456"))
                .verifyComplete();

        verify(transactionService, times(1)).transfer("123", "456", 200.0);
    }

    @Test
    void transfer_shouldHandleErrorFromService() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setSourceAccountId("123");
        request.setDestinationAccountId("456");
        request.setAmount(200.0);

        when(transactionService.transfer("123", "456", 200.0))
                .thenReturn(Mono.error(new RuntimeException("Transfer service failed")));

        // Act
        Mono<TransactionResponseDto> result = transactionController.transfer(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Transfer service failed"))
                .verify();

        verify(transactionService, times(1)).transfer("123", "456", 200.0);
    }

    @Test
    void transfer_shouldMapResponseCorrectly() {
        // Arrange
        TransactionRequestDto request = new TransactionRequestDto();
        request.setSourceAccountId("123");
        request.setDestinationAccountId("456");
        request.setAmount(200.0);

        Transaction transaction = Transaction.builder()
                .id("txn789")
                .type(TransactionType.TRANSFER)
                .amount(200.0)
                .date(LocalDateTime.now())
                .sourceAccountId("123")
                .destinationAccountId("456")
                .build();

        when(transactionService.transfer("123", "456", 200.0)).thenReturn(Mono.just(transaction));

        // Act
        Mono<TransactionResponseDto> result = transactionController.transfer(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getId().equals("txn789") &&
                                response.getType().equals("TRANSFER") &&
                                response.getAmount().equals(200.0) &&
                                response.getDate() != null &&
                                response.getSourceAccountId().equals("123") &&
                                response.getDestinationAccountId().equals("456"))
                .verifyComplete();

        verify(transactionService, times(1)).transfer("123", "456", 200.0);
    }

    @Test
    void getGlobalTransactionHistory_shouldReturnEmptyFlux() {
        // Arrange
        when(transactionService.getGlobalTransactionHistory()).thenReturn(Flux.empty());

        // Act
        Flux<TransactionResponseDto> result = transactionController.getGlobalTransactionHistory();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(transactionService, times(1)).getGlobalTransactionHistory();
    }

    @Test
    void getGlobalTransactionHistory_shouldHandleServiceError() {
        // Arrange
        when(transactionService.getGlobalTransactionHistory())
                .thenReturn(Flux.error(new RuntimeException("Service failed")));

        // Act
        Flux<TransactionResponseDto> result = transactionController.getGlobalTransactionHistory();

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Service failed"))
                .verify();

        verify(transactionService, times(1)).getGlobalTransactionHistory();
    }

    @Test
    void getAccountTransactionHistory_shouldReturnEmptyFlux() {
        // Arrange
        String accountId = "123";

        when(transactionService.getAccountTransactionHistory(accountId)).thenReturn(Flux.empty());

        // Act
        Flux<TransactionResponseDto> result = transactionController.getAccountTransactionHistory(accountId);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(transactionService, times(1)).getAccountTransactionHistory(accountId);
    }

    @Test
    void getAccountTransactionHistory_shouldHandleServiceError() {
        // Arrange
        String accountId = "123";

        when(transactionService.getAccountTransactionHistory(accountId))
                .thenReturn(Flux.error(new RuntimeException("Service failed")));

        // Act
        Flux<TransactionResponseDto> result = transactionController.getAccountTransactionHistory(accountId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Service failed"))
                .verify();

        verify(transactionService, times(1)).getAccountTransactionHistory(accountId);
    }
}
