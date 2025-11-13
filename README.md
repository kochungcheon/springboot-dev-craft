# Spring Boot Dev Craft

A ready-to-use boilerplate for building reproducible Spring Boot applications. This project provides a fully containerized environment for both development and testing, ensuring that "it works on my machine" becomes "it works everywhere."

## Features

- **Spring Boot 3**: A simple health check API (`GET /health`).
- **Containerized Services**: `docker-compose.yml` to instantly launch MySQL, Redis, and Kafka.
- **Reproducible Builds**: A multi-stage `Dockerfile` for creating optimized, lightweight application images.
- **Hermetic Testing**: Integration tests built with **Testcontainers** and **Cucumber**, ensuring tests run in a clean, isolated environment without depending on external services.

## Tech Stack

- **Framework**: Spring Boot 3.5
- **Language**: Java 17
- **Database**: MySQL 8.4
- **Cache**: Redis 7.2
- **Message Broker**: Kafka 7.5 (Confluent Platform)
- **Containerization**: Docker, Docker Compose
- **Testing**: Cucumber 7.16, Testcontainers 1.20, JUnit 5
- **Build Tool**: Gradle

## Prerequisites

- Java 17 (or later)
- Docker Desktop

## Running the System

The entire application stack (Spring Boot App, MySQL, Redis, Kafka) can be launched with a single command.

1. **Build and Run the services in detached mode:**
   ```bash
   docker-compose up --build -d
   ```

2. **Verify the application is running:**
   Open your browser or use a tool like `curl` to access the health check endpoint.
   ```bash
   curl http://localhost:8080/health
   ```
   You should receive an "OK" response.

## Running the Tests

This project uses Testcontainers to programmatically launch and manage Docker containers for integration tests. This ensures that tests are reliable and independent of the `docker-compose` setup.

To run the entire test suite, execute the following command:

```bash
./gradlew test
```

This command will compile the code, download dependencies, and run all the Cucumber scenarios defined in `src/test/resources`.
