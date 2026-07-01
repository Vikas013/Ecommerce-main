# Production-Grade E-Commerce Microservices Platform

This repository serves as a portfolio showcase demonstrating the design, implementation, and operation of a **distributed, cloud-native microservices architecture**. Built with **Java 17**, **Spring Boot 3.x**, **Spring Cloud**, **Apache Kafka**, and **Docker**, it showcases production-ready patterns for scalability, fault tolerance, security, and observability.

The design of this system aligns directly with the architectural competencies, clean coding standards, and platform reliability principles required for a **Senior Software Engineer (Platform Engineering)** role.

---

## 🏛️ Architectural Overview

The platform is designed around a decentralized microservices model where services are isolated by domain, communicate via lightweight protocols (REST and Event-Driven Messaging), and scale independently.

```
                  ┌────────────────────────┐
                  │    Client / Frontend   │
                  └───────────┬────────────┘
                              │ HTTPS (Port 8080)
                              ▼
                  ┌────────────────────────┐
                  │      API Gateway       │◄─────┐
                  │ (Spring Cloud Gateway) │      │ OAuth2
                  └───────────┬────────────┘      │ JWT Auth
                              │                   ▼
             ┌────────────────┼────────────────┬──┴──────────────────┐
             │ (Path: /api/product)            │ (Path: /api/order)  │ (Path: /eureka/**)
             ▼                                 ▼                     ▼
 ┌───────────────────────┐         ┌───────────────────────┐   ┌───────────────────────┐
 │    Product Service    │         │     Order Service     │   │   Discovery Server    │
 │ (MongoDB / Dyn Port)  │         │  (MySQL / Port 8081)  │   │  (Eureka / Port 8761) │
 └───────────────────────┘         └───────────┬───────────┘   └───────────────────────┘
                                               │
                                               │ Sync HTTP GET
                                               │ (WebClient + Resilience4j)
                                               ▼
                                   ┌───────────────────────┐
                                   │   Inventory Service   │
                                   │ (MySQL / Dynamic Port)│
                                   └───────────┬───────────┘
                                               │
                                               │ Async Event (OrderPlacedEvent)
                                               ▼
                                   ┌───────────────────────┐
                                   │     Apache Kafka      │
                                   │      (Port 9092)      │
                                   └───────────┬───────────┘
                                               │
                                               ▼
                                   ┌───────────────────────┐
                                   │ Notification Service  │
                                   │ (Java / Dynamic Port) │
                                   └───────────────────────┘
```

---

## 🛠️ Showcase of Key Competencies

### 1. Distributed Systems & Microservice Architecture
* **Service Discovery**: Implemented via **Spring Cloud Netflix Eureka** (`discovery-server`). Services register dynamically, allowing them to bind to random ports (`server.port=0`) and scale horizontally without hardcoded endpoints.
* **Smart Routing & API Gateway**: **Spring Cloud Gateway** (`api-gateway`) acts as the single entry point. It dynamically resolves downstream routes using Eureka IDs (`lb://product-service`, `lb://order-service`) and manages load balancing.
* **Hybrid Communication Model**:
  * **Synchronous REST**: The `order-service` calls the `inventory-service` synchronously via WebClient to ensure inventory checks are blocking and atomic before confirming orders.
  * **Asynchronous Event-Driven**: Uses **Apache Kafka** to broadcast `OrderPlacedEvent` asynchronously to decouple the order placement flow from downstream side-effects (e.g., sending emails/notifications).

### 2. System Reliability & Fault Tolerance (Resilience4j)
To prevent cascading failures and maintain high availability in production, the [OrderController](file:///Users/vikasjain/AIProjects/Ecommerce-main/order-service/src/main/java/com/ecommerce/order/controller/OrderController.java) encapsulates synchronous remote inventory calls with **Resilience4j** decorators:
* **Circuit Breaker**: Monitored using a sliding-window failure rate threshold (50% failure triggers the circuit to open, instantly failing fast).
* **Time Limiter**: Enforces a strict `3s` timeout on synchronous calls to prevent thread exhaustion.
* **Retry Pattern**: Automatically retries failed requests up to `3` times with a `5s` backoff interval.
* **Graceful Fallback**: Defers to a fallback handler to return a friendly user message during degradation, protecting the main thread pool.

### 3. Production-Grade Observability (Distributed Tracing)
Every service integrates **Brave / Micrometer Tracing** and reports traces to a centralized **Zipkin** instance (`http://localhost:9411`).
* **Telemetry Propagation**: Outgoing WebClient HTTP headers and incoming Kafka message envelopes are injected/extracted with W3C trace contexts.
* **Root Cause Analysis**: Allows engineers to track latency and diagnose performance bottlenecks across multiple network hops using a single correlation Trace ID.

### 4. Enterprise Security
* **Gateway-Level Security**: Secured via a reactive WebFlux security filter chain in the API Gateway.
* **OAuth2 / OIDC Integration**: Interacts with **Keycloak** identity provider for token-based authentication.
* **Granular Auth Policies**: Enforces token validation for domain routes (`anyExchange().authenticated()`) while exposing the service registry (`/eureka/**`) to allow inner-cluster communication.

### 5. Cloud-Native & Containerization
* **Jib Containerization**: Uses `jib-maven-plugin` in the parent [pom.xml](file:///Users/vikasjain/AIProjects/Ecommerce-main/pom.xml) to build highly optimized Docker images directly from source. By avoiding heavy local Dockerfiles, Jib ensures reproducible builds and JRE-only, secure image layers.
* **Docker Compose Orchestration**: Configured to instantiate Apache Kafka and Zookeeper broker dependencies for seamless local deployment.

---

## 📂 Project Directory Structure

```bash
├── api-gateway            # API Gateway with Spring Security WebFlux & Keycloak OAuth2
├── discovery-server       # Netflix Eureka Service Discovery Server
├── product-service        # MongoDB database, product CRUD operations
├── order-service          # MySQL database, Resilience4j circuit breakers, Kafka event producer
├── inventory-service      # MySQL database, checks and maintains stock quantities
├── notification-service   # Kafka event consumer, handles asynchronous user alerts
├── docker-compose.yml     # Starts Zookeeper & Kafka Broker
└── pom.xml                # Multi-module parent Maven configuration
```

---

## 🚀 Local Execution & Setup

### Prerequisites
* JDK 17
* Maven 3.8+
* Docker & Docker Compose
* Running MySQL instance (port `3306`) and MongoDB instance (port `27017`)

### 1. Build the Microservices
From the root directory, compile and package the modules:
```bash
mvn clean package -DskipTests
```

### 2. Start Infrastructure Services
Spin up Kafka and Zookeeper:
```bash
docker-compose up -d
```

### 3. Run Microservices (Recommended Order)
Run the services in the following order to allow proper registration:
1. **Discovery Server**: `mvn spring-boot:run` within `/discovery-server` (Port: `8761`)
2. **Product Service**: `mvn spring-boot:run` within `/product-service` (Dynamic Port)
3. **Inventory Service**: `mvn spring-boot:run` within `/inventory-service` (Dynamic Port)
4. **Order Service**: `mvn spring-boot:run` within `/order-service` (Port: `8081`)
5. **Notification Service**: `mvn spring-boot:run` within `/notification-service` (Dynamic Port)
6. **API Gateway**: `mvn spring-boot:run` within `/api-gateway` (Port: `8080`)

---

## 🎯 Clean Coding & Design Standards Demonstrated
* **DTO Pattern**: Clear decoupling of database entities from REST contracts (e.g. `ProductRequestDto`, `ProductResponseDto`).
* **Strict Separation of Concerns**: Data schemas are isolated (MongoDB for document-oriented catalog data, MySQL for highly consistent transactional order/inventory data).
* **Asynchronous Non-blocking Actions**: Leverages Java's `CompletableFuture` for asynchronous execution inside HTTP controllers where appropriate.
