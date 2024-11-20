
# Transaction Microservice

A microservice for managing bank transactions, including deposits, withdrawals, transfers, and transaction history.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Configuration](#configuration)
- [Integration with Other Microservices](#integration-with-other-microservices)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [License](#license)

---

## Features
- Perform deposits to a bank account.
- Withdraw funds from a bank account with balance validation.
- Transfer funds between two bank accounts.
- Retrieve global transaction history.
- Retrieve transaction history for a specific account.

---

## Technologies Used
- **Java 11**
- **Spring Boot** (Reactive Web, MongoDB, WebFlux)
- **MongoDB** (Database)
- **Lombok** (To reduce boilerplate code)
- **OpenAPI 3.0** (API Documentation)
- **WebClient** (For communication with other microservices)
- **Maven** (Dependency management)

---

## Installation

### Prerequisites
1. Install **Java 11** or higher.
2. Install **Maven**.
3. Set up **MongoDB** (local or cloud).

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/BrigitteMendez1302/transaction-microservice.git
   cd transaction-microservice
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Configure the application properties (see [Configuration](#configuration)).

---

## Configuration

Modify the `application.yml` or `application.properties` file to match your environment:

```properties
# Spring Application Configuration
spring.application.name=transactionmicroservice

# Server Configuration
server.port=8085

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=microservicetransactionsdb

# Microservices Configuration
microservices.bank-accounts.base-url=http://localhost:8082/api
```
---


## Integration with Other Microservices

This microservice connects to the **Bank Account Microservice** to perform operations such as deposits, withdrawals, and transfers. The integration is implemented using **WebClient**, a reactive HTTP client provided by Spring WebFlux.

### Endpoints Used in Bank Account Microservice

The following endpoints are used by the Transaction Microservice to interact with the Bank Account Microservice:

| Endpoint                              | Method | Description                                    |
|---------------------------------------|--------|------------------------------------------------|
| `/accounts/{id}`                      | GET    | Retrieves the details of a specific account.   |
| `/accounts/{id}/deposit?amount={amount}` | PUT    | Deposits a specified amount into an account.   |
| `/accounts/{id}/withdraw?amount={amount}` | PUT    | Withdraws a specified amount from an account. |

### Configuration

The connection to the **Bank Account Microservice** is configured dynamically using the `application.properties` file. The base URL for the Bank Account Microservice is defined as follows:

```properties
microservices.bank-accounts.base-url=http://localhost:8082/api
```

This configuration allows the Transaction Microservice to communicate effectively with the Bank Account Microservice without hardcoding the base URL.

### Bank Account Microservice Repository

The Bank Account Microservice is available in the following repository:

[Bank Account Microservice Repository](https://github.com/BrigitteMendez1302/Java-Proyecto2-BankAccountMicroservice)


---

## Running the Application

### Locally
Start the application with Maven:
```bash
mvn spring-boot:run
```

### Docker (Optional)
If you have a Dockerfile, build and run the container:
```bash
docker build -t transaction-microservice .
docker run -p 8085:8085 transaction-microservice
```

---

## API Documentation

The API is documented using **OpenAPI 3.0**. These are the endpoints
```
http://localhost:8085/swagger-ui.html
```

### Key Endpoints
| Endpoint                            | Method | Description                                    |
|-------------------------------------|--------|------------------------------------------------|
| `/transactions/deposit`             | POST   | Create a deposit transaction.                 |
| `/transactions/withdraw`            | POST   | Create a withdrawal transaction.              |
| `/transactions/transfer`            | POST   | Create a transfer transaction.                |
| `/transactions`                     | GET    | Retrieve global transaction history.          |
| `/transactions/account/{accountId}` | GET    | Retrieve transaction history for a given account.|

---

## Project Structure
```plaintext
src/
├── main/
│   ├── java/
│   │   └── com.example.transactionmicroservice/
│   │       ├── client/        # WebClient for interacting with Bank Account microservice
│   │       ├── config/        # Files for configuration
│   │       ├── controller/    # REST controllers
│   │       ├── model/         # Entity models
│   │       ├── repository/    # Repositories for MongoDB
│   │       ├── service/       # Service layer
│   │       └── TransactionMicroserviceApplication.java # Main application
│   └── resources/
│       ├── application.properties    # Application configuration
│       ├── api.yml                   # Open API Documentation
│       ├── static/            # Static files (if any)
│       └── templates/         # Templates for views (if any)
└── test/
    └── java/                  # Unit and integration tests
```

---

## License
This project is licensed under the [MIT License](LICENSE).
