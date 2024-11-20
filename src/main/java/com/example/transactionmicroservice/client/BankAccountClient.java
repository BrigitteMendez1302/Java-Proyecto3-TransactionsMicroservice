package com.example.transactionmicroservice.client;

import com.example.transactionmicroservice.model.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountClient {

    private final WebClient webClient; // WebClient utilizado para interactuar con el microservicio de cuentas bancarias.

    /**
     * Retrieves the details of a bank account by its ID.
     *
     * @param accountId The ID of the account to retrieve.
     * @return A Mono containing the details of the bank account.
     */
    public Mono<BankAccount> getAccount(String accountId) {
        return webClient.get()
                .uri("/accounts/{id}", accountId) // Construye la URI del endpoint para obtener una cuenta
                .retrieve() // Envía la solicitud y espera la respuesta
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        // Maneja los errores devueltos por el microservicio
                        response.bodyToMono(String.class) // Lee el cuerpo del error como una cadena
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(
                                        response.statusCode(),
                                        "Error fetching account: " + errorBody
                                )))
                )
                .bodyToMono(BankAccount.class); // Convierte la respuesta en un objeto BankAccount
    }

    /**
     * Performs a deposit to a specified bank account.
     *
     * @param accountId The ID of the account where the deposit will be made.
     * @param amount    The amount to deposit.
     * @return A Mono containing the updated bank account after the deposit.
     */
    public Mono<BankAccount> deposit(String accountId, Double amount) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/accounts/{id}/deposit") // Construye la URI del endpoint para depósitos
                        .queryParam("amount", amount) // Agrega el monto como parámetro de consulta
                        .build(accountId)) // Reemplaza el placeholder {id} con accountId
                .retrieve() // Envía la solicitud y espera la respuesta
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        // Maneja los errores devueltos por el microservicio
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(
                                        response.statusCode(),
                                        "Error depositing: " + errorBody
                                )))
                )
                .bodyToMono(BankAccount.class); // Convierte la respuesta en un objeto BankAccount
    }

    /**
     * Performs a withdrawal from a specified bank account.
     *
     * @param accountId The ID of the account from which the withdrawal will be made.
     * @param amount    The amount to withdraw.
     * @return A Mono containing the updated bank account after the withdrawal.
     */
    public Mono<BankAccount> withdraw(String accountId, Double amount) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/accounts/{id}/withdraw") // Construye la URI del endpoint para retiros
                        .queryParam("amount", amount) // Agrega el monto como parámetro de consulta
                        .build(accountId)) // Reemplaza el placeholder {id} con accountId
                .retrieve() // Envía la solicitud y espera la respuesta
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        // Maneja los errores devueltos por el microservicio
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(
                                        response.statusCode(),
                                        "Error withdrawing: " + errorBody
                                )))
                )
                .bodyToMono(BankAccount.class); // Convierte la respuesta en un objeto BankAccount
    }
}
