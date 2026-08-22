# ShopSphere — Project Context & Engineering Plan

> This document captures the decisions, goals, architecture, tooling, development process, and roadmap agreed upon for the ShopSphere project.
>
> It is intended to preserve the context of the project so development can continue consistently.

---

# 1. Project Overview

## 1.1 Project Name

**ShopSphere**

## 1.2 Project Type

Scalable e-commerce platform built as an end-to-end software engineering learning project.

The purpose is not simply to build an e-commerce application. The purpose is to learn how a modern production-grade software system is:

- Conceived
- Documented
- Designed
- Developed
- Tested
- Secured
- Deployed
- Monitored
- Scaled
- Maintained

---

# 2. Primary Learning Objectives

The project should provide practical experience with:

- Software requirements
- BRD
- Functional and non-functional requirements
- Agile/Scrum
- Jira
- Git/GitHub
- Java
- Spring Boot
- REST APIs
- Microservices
- Clean Architecture
- Hexagonal Architecture
- SOLID principles
- Design patterns
- PostgreSQL
- Redis
- Kafka
- React
- TypeScript
- Docker
- Kubernetes
- Helm
- GitHub Actions
- CI/CD
- Secrets management
- HashiCorp Vault
- Authentication
- Authorization
- Observability
- Logging
- Metrics
- Distributed tracing
- Resilience
- Scalability
- Testing
- Integration testing
- Contract testing
- Load testing
- Production deployment

---

# 3. Core Learning Philosophy

We will not introduce technology merely because it is popular.

For every significant technology or architectural decision:

1. Identify the problem.
2. Understand the requirements.
3. Consider alternatives.
4. Select a solution.
5. Document the decision.
6. Implement it.
7. Measure/validate the result.

Example:

We will not use Kafka simply because Kafka is part of modern architectures.

Instead:

```text
Business requirement
        ↓
Multiple independent consumers
        ↓
Tight coupling becomes a problem
        ↓
Evaluate synchronous vs asynchronous communication
        ↓
Kafka selected
        ↓
ADR created
        ↓
Kafka implemented
````

This principle applies to Redis, Kubernetes, microservices, caching, event-driven architecture, etc.

---

# 4. Business Domain

ShopSphere is an e-commerce platform.

## Customer journey

```text
Register
   ↓
Login
   ↓
Browse products
   ↓
View product
   ↓
Add product to cart
   ↓
Checkout
   ↓
Payment
   ↓
Order confirmation
   ↓
Track order
```

## Administrator capabilities

```text
Manage products
Manage categories
Manage inventory
View orders
Manage users
```

---

# 5. Initial Functional Scope

## Identity

* User registration
* Login
* Password hashing
* Authentication
* Authorization
* Roles
* Permissions
* Refresh/session strategy

## Product Catalog

* Create product
* Get product
* List products
* Update product
* Deactivate product
* Categories
* Pagination
* Search

## Cart

* Create/retrieve cart
* Add item
* Remove item
* Change quantity
* Cart expiration
* Redis-backed cart state

## Orders

* Checkout
* Create order
* Order lifecycle
* Order state machine
* Order history
* Idempotency

## Inventory

* Stock management
* Stock reservation
* Stock release
* Stock commit
* Concurrency protection
* Overselling prevention

## Payment

Initially a mock payment provider will be used.

Capabilities:

* Payment abstraction
* Payment initiation
* Payment lifecycle
* Success/failure
* Refund
* Idempotency

## Notifications

* Order confirmation
* Payment notification
* Inventory-related notifications
* Asynchronous processing
* Retry
* Dead-letter handling

---

# 6. Non-Functional Requirements

## Scalability

Services should be independently horizontally scalable.

Example:

```text
Product Service   × 5
Order Service     × 10
Notification      × 3
```

Scaling one service should not require scaling the entire application.

## Availability

The system should tolerate failures where practical.

Failure of a non-critical asynchronous service should not unnecessarily prevent core business transactions.

## Resilience

We will progressively introduce:

* Timeouts
* Retries
* Idempotency
* Circuit breakers
* Dead-letter queues/topics
* Health checks
* Readiness checks
* Graceful degradation

## Security

* Passwords must never be stored in plaintext.
* Passwords must not be reversibly encrypted.
* Passwords should use a modern password hashing algorithm such as Argon2id.
* Secrets must not be committed to Git.
* Database credentials must be stored in a credential manager.
* API keys must be stored in a credential manager.
* JWT signing secrets must be protected.
* Least privilege should be followed.

## Observability

The system should provide:

* Structured logs
* Metrics
* Distributed traces
* Correlation/request IDs
* Health information
* Dashboards
* Alerts

## Maintainability

Services should have:

* Clear boundaries
* High cohesion
* Low coupling
* Explicit contracts
* Automated tests
* Documented architectural decisions

---

# 7. Target Architecture

The target architecture is microservice based.

```text
                         ┌──────────────┐
                         │   Customer   │
                         └──────┬───────┘
                                │
                                ▼
                         ┌──────────────┐
                         │    React     │
                         └──────┬───────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   API Gateway   │
                       └────────┬────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 ▼                 ▼
       Identity Service   Product Service    Cart Service
              │                 │                 │
              ▼                 ▼                 ▼
        Identity DB        Product DB           Redis

                                │
                                ▼
                         Order Service
                          /           \
                         ▼             ▼
                  Inventory        Payment
                      │               │
                      ▼               ▼
                  Inventory DB     Payment DB

                                │
                                ▼
                              Kafka
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
              Notification   Analytics    Future
```

This is the target architecture.

It will NOT be implemented all at once.

We will evolve toward it incrementally.

---

# 8. Planned Microservices

## API Gateway

Responsibilities:

* Entry point for frontend
* Routing
* Authentication-related gateway concerns
* Rate limiting where appropriate
* Request correlation

## Identity Service

Responsibilities:

* Registration
* Authentication
* Password management
* User identity
* Roles/permissions

## Product Service

Responsibilities:

* Product catalog
* Categories
* Product lifecycle
* Product search

## Cart Service

Responsibilities:

* Shopping cart
* Cart items
* Cart expiration

Redis will be used heavily here.

## Order Service

Responsibilities:

* Checkout
* Order creation
* Order lifecycle
* Order history
* Idempotency

## Inventory Service

Responsibilities:

* Stock
* Reservation
* Release
* Commit
* Concurrency

## Payment Service

Responsibilities:

* Payment abstraction
* Payment lifecycle
* Provider integration
* Refunds

Initially a mock provider will be used.

## Notification Service

Responsibilities:

* Email notifications
* Event consumption
* Retry
* Dead-letter handling

---

# 9. Technology Stack

## Backend

```text
Java 21 LTS
Spring Boot
Spring Security
Spring Data
Spring Validation
Spring Kafka
Flyway
OpenAPI
```

## Frontend

```text
React
TypeScript
Node.js 24 LTS
npm
```

## Databases

### PostgreSQL

Primary transactional database.

Suitable for:

* Users
* Products
* Orders
* Inventory
* Payments

### Redis

Used for:

* Cart
* Caching
* Temporary/ephemeral state
* TTL-based data

### Kafka

Used for:

* Domain/integration events
* Asynchronous communication
* Decoupled consumers
* Event-driven workflows

---

# 10. Secrets Management

HashiCorp Vault will be used as the credential/secrets management solution.

Secrets include:

* Database passwords
* API keys
* JWT signing secrets
* External service credentials
* Private keys

Never commit secrets to Git.

Bad:

```text
spring.datasource.password=MyPassword123
```

Good:

```text
Application
     ↓
Vault
     ↓
Secret
```

---

# 11. Infrastructure

## Local Development

Initially:

```text
React                Local
Spring Boot          Local
PostgreSQL           Docker
Redis                Docker
Kafka                Docker
Vault                Docker
```

Docker Compose will manage local infrastructure dependencies.

Later the complete application will be containerized.

---

# 12. Containerization

Each deployable service should eventually have its own Docker image.

Example:

```text
shopsphere/product-service
shopsphere/order-service
shopsphere/inventory-service
shopsphere/payment-service
```

Images will eventually be pushed to a container registry.

---

# 13. Kubernetes

Kubernetes is the target deployment platform.

We will learn:

* Namespace
* Deployment
* Service
* ConfigMap
* Secret
* Ingress/Gateway
* Resource requests
* Resource limits
* Liveness probes
* Readiness probes
* Startup probes
* Horizontal Pod Autoscaler
* Rolling deployments
* Rollbacks

---

# 14. Helm

Helm will package Kubernetes deployments.

Conceptually:

```text
helm/
└── shopsphere/
    ├── Chart.yaml
    ├── values.yaml
    ├── templates/
    └── values/
```

We will eventually support environment-specific configuration.

---

# 15. CI/CD

GitHub Actions will initially be used.

## Pull Request pipeline

```text
Pull Request
      ↓
Compile
      ↓
Unit Tests
      ↓
Integration Tests
      ↓
Static Analysis
      ↓
Security Checks
      ↓
Build Docker Image
```

## Deployment pipeline

Eventually:

```text
Merge
  ↓
Build
  ↓
Test
  ↓
Security Scan
  ↓
Build Docker Image
  ↓
Push Image
  ↓
Helm Deployment
  ↓
Kubernetes
  ↓
Smoke Tests
```

Rollback capability will be implemented.

---

# 16. Observability

We will use:

```text
OpenTelemetry
Prometheus
Grafana
```

The goal is to understand:

```text
What happened?
Why did it happen?
Which service failed?
How long did it take?
Which downstream service caused the problem?
```

Example:

```text
Order Request
      │
      ▼
API Gateway
      │
      ▼
Order Service
      │
      ├── Inventory
      │
      ├── Payment
      │
      └── Kafka
              │
              └── Notification
```

A distributed trace should allow us to follow the request/event flow.

---

# 17. Architecture Principles

## SOLID

We will actively apply:

* Single Responsibility Principle
* Open/Closed Principle
* Liskov Substitution Principle
* Interface Segregation Principle
* Dependency Inversion Principle

## Additional principles

* Separation of concerns
* Composition over inheritance
* Explicit dependencies
* Dependency inversion
* High cohesion
* Low coupling
* Fail fast where appropriate
* Secure by default
* Design for testability
* Prefer simple solutions
* Avoid premature optimization

---

# 18. Architecture Style

We will use a pragmatic combination of:

* Microservices
* Clean Architecture concepts
* Hexagonal Architecture / Ports and Adapters
* Domain-driven design concepts where useful
* Event-driven architecture where justified

We will NOT blindly implement every DDD pattern.

The goal is to understand when each pattern is useful.

---

# 19. Typical Service Structure

Example:

```text
product-service/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/shopsphere/product/
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   ├── repository/
│   │   │       │   └── exception/
│   │   │       │
│   │   │       ├── application/
│   │   │       │   ├── service/
│   │   │       │   └── port/
│   │   │       │
│   │   │       ├── adapter/
│   │   │       │   ├── in/
│   │   │       │   │   └── web/
│   │   │       │   └── out/
│   │   │       │       └── persistence/
│   │   │       │
│   │   │       └── config/
│   │   │
│   │   └── resources/
│   │       └── db/
│   │           └── migration/
│   │
│   └── test/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

Dependency direction:

```text
Adapters
   ↓
Application
   ↓
Domain
```

The domain should not depend directly on:

* PostgreSQL
* Redis
* Kafka
* HTTP
* Spring-specific infrastructure

where that dependency can reasonably be avoided.

---

# 20. Database Strategy

We will prefer database-per-service ownership.

Conceptually:

```text
Identity Service
       ↓
Identity DB

Product Service
       ↓
Product DB

Order Service
       ↓
Order DB

Inventory Service
       ↓
Inventory DB

Payment Service
       ↓
Payment DB
```

We will avoid one giant shared database schema across all services.

The physical deployment strategy may evolve during development, but logical ownership remains separated.

---

# 21. Database Migrations

Flyway will be used.

Example:

```text
V1__create_products.sql
V2__add_product_status.sql
V3__add_product_indexes.sql
```

Database schema changes must be version controlled.

---

# 22. API Design

REST will initially be the primary synchronous API mechanism.

APIs should have:

* Clear resources
* Proper HTTP methods
* Proper status codes
* Validation
* Error responses
* Pagination
* Filtering
* Versioning strategy where appropriate
* OpenAPI documentation

Persistence entities should not be exposed directly as API contracts.

Use DTOs.

---

# 23. Kafka Strategy

Kafka will be introduced when asynchronous communication is justified.

Potential events:

```text
OrderCreated
OrderConfirmed
OrderCancelled

InventoryReserved
InventoryReleased
InventoryCommitted

PaymentInitiated
PaymentSucceeded
PaymentFailed

NotificationRequested
```

Topics, schemas, retry strategy, dead-letter topics and schema evolution will be designed deliberately.

We will eventually study:

* Producer
* Consumer
* Consumer groups
* Partitions
* Offsets
* Ordering
* Delivery semantics
* Retry
* Dead-letter topics
* Schema evolution
* Outbox pattern

---

# 24. Redis Strategy

Redis will initially support the shopping cart.

Example:

```text
cart:{userId}
```

with a TTL.

Redis may also be used later for:

* Caching
* Rate limiting
* Distributed coordination where justified

Redis will NOT replace PostgreSQL for transactional business data without a specific architectural reason.

---

# 25. Security Principles

Security is part of the design rather than a final phase.

We will consider:

* Authentication
* Authorization
* Password hashing
* Secret management
* TLS
* Input validation
* Secure headers
* Rate limiting
* Least privilege
* Audit logging
* Dependency scanning
* Container scanning
* Kubernetes security

---

# 26. Git Strategy

Repository:

```text
shopsphere
```

Initially this will be a monorepo.

Example:

```text
shopsphere/
├── apps/
├── infrastructure/
├── docs/
├── scripts/
└── .github/
```

## Branches

Feature branches:

```text
feature/SHOP-42-product-creation
```

Bug:

```text
fix/SHOP-73-duplicate-sku
```

Documentation:

```text
docs/SHOP-10-architecture
```

Refactoring:

```text
refactor/SHOP-50-product-domain
```

---

# 27. Commit Convention

We will use Conventional Commits.

Examples:

```text
feat(product): add product creation API

fix(order): prevent duplicate order creation

test(product): add repository integration tests

refactor(payment): extract payment provider adapter

docs(architecture): document order workflow

chore(build): upgrade Spring Boot
```

Jira issue IDs should be included where useful.

Example:

```text
feat(SHOP-42): add product creation API
```

---

# 28. Pull Request Strategy

A Pull Request should contain:

* Jira issue
* Problem description
* Solution
* Testing performed
* Architectural considerations
* Screenshots where applicable

Example:

```text
SHOP-42 Implement Product Creation API

Problem:
Administrators need to create products.

Solution:
Added Product domain, application service and REST adapter.

Testing:
- Unit tests
- Repository integration test
- API integration test
```

---

# 29. Jira

Jira Cloud is the project-management platform.

Project:

```text
ShopSphere
```

Key:

```text
SHOP
```

We will use Scrum.

## Issue types

* Epic
* Story
* Task
* Bug
* Sub-task

Additional types such as Spike or Technical Debt can be introduced later if justified.

## Workflow

Initially:

```text
TO DO
   ↓
IN PROGRESS
   ↓
CODE REVIEW
   ↓
QA
   ↓
DONE
```

We deliberately do not want a complicated workflow at the beginning.

---

# 30. Jira Epic Structure

## EPIC — Project Foundation

* Repository
* Documentation
* Development environment
* Docker Compose
* Initial CI

## EPIC — Identity & Security

* Registration
* Login
* Authentication
* Authorization
* Password hashing
* Vault

## EPIC — Product Catalog

* Products
* Categories
* Search
* Pagination
* Administration

## EPIC — Shopping Cart

* Cart
* Cart items
* Redis
* TTL
* Concurrency

## EPIC — Order Management

* Checkout
* Order creation
* Order lifecycle
* Idempotency

## EPIC — Inventory

* Stock
* Reservation
* Release
* Commit
* Concurrency

## EPIC — Payment

* Payment abstraction
* Mock provider
* Payment lifecycle
* Refund
* Idempotency

## EPIC — Event Platform

* Kafka
* Events
* Consumers
* Retry
* DLT
* Outbox

## EPIC — Notifications

* Email
* Async processing
* Retry

## EPIC — Frontend

* React
* Authentication
* Product catalog
* Cart
* Checkout
* Orders
* Admin

## EPIC — Observability

* Logging
* Metrics
* Tracing
* Prometheus
* Grafana
* OpenTelemetry

## EPIC — CI/CD

* GitHub Actions
* Build
* Test
* Security scanning
* Docker
* Deployment

## EPIC — Kubernetes

* Deployments
* Services
* ConfigMaps
* Secrets
* Ingress
* HPA
* Health checks

## EPIC — Helm

* Helm chart
* Environment values
* Deployment templates

## EPIC — Production Hardening

* Rate limiting
* Resilience
* Caching
* Load testing
* Failure testing
* Disaster recovery
* Security hardening

---

# 31. Sprint 1

## Sprint Name

```text
Sprint 1 — Engineering Foundation
```

## Objective

Create a reproducible development environment and engineering foundation.

## Initial backlog

```text
SHOP-1   Create GitHub repository
SHOP-2   Create repository structure
SHOP-3   Create engineering README
SHOP-4   Create BRD
SHOP-5   Create architecture documentation
SHOP-6   Establish ADR process
SHOP-7   Configure Java/Maven
SHOP-8   Configure React/TypeScript
SHOP-9   Create Docker Compose
SHOP-10  Run PostgreSQL locally
SHOP-11  Run Redis locally
SHOP-12  Run Kafka locally
SHOP-13  Configure GitHub Actions
SHOP-14  Create Product Service skeleton
SHOP-15  Create initial database migration
SHOP-16  Validate local development architecture
```

---

# 32. First Engineering Ticket

## SHOP-1 — Create GitHub Repository

### Description

Create the ShopSphere monorepository containing:

* React frontend
* Spring Boot microservices
* Infrastructure
* Kubernetes/Helm configuration
* CI/CD workflows
* Documentation
* ADRs
* Operational documentation

### Acceptance Criteria

Given the repository exists:

* A developer can clone it.
* README exists.
* .gitignore exists.
* .editorconfig exists.
* Documentation directories exist.
* No secrets are committed.

### Definition of Done

```text
☐ Repository created
☐ README created
☐ .gitignore created
☐ .editorconfig created
☐ Directory structure created
☐ Initial commit created
☐ Repository pushed to GitHub
☐ Jira issue linked to commit
```

---

# 33. Documentation Strategy

Documentation is part of the product.

Planned documentation:

```text
docs/
├── 00-project-overview/
├── 01-business/
├── 02-requirements/
├── 03-architecture/
├── 04-architecture-decisions/
├── 05-development/
├── 06-security/
├── 07-infrastructure/
├── 08-cicd/
├── 09-learning-log/
└── jira/
```

Documentation types:

* BRD
* Functional requirements
* Non-functional requirements
* HLD
* LLD
* Architecture diagrams
* ADRs
* API documentation
* Security documentation
* Deployment documentation
* Runbooks
* Development standards
* Troubleshooting guides
* Learning log

---

# 34. ADR Process

Architectural Decision Records will be used for meaningful architectural choices.

Format:

```text
# ADR-XXX: Decision Title

## Status

Proposed / Accepted / Deprecated / Superseded

## Context

What problem are we solving?

## Decision

What did we choose?

## Alternatives

What else did we consider?

## Rationale

Why did we choose this?

## Consequences

What are the benefits and drawbacks?
```

---

# 35. Existing Architecture Decisions

## ADR-001 — Jira Cloud

### Decision

Use Jira Cloud.

### Reason

We want professional project-management workflow experience without spending project effort operating a project-management platform.

---

## ADR-002 — Monorepo

### Decision

Use a monorepository initially.

### Reason

* Easier cross-service changes
* Easier learning
* Single documentation/code location
* Simple CI/CD coordination

This can be revisited later.

---

## ADR-003 — PostgreSQL

### Decision

Use PostgreSQL for transactional relational data.

### Reason

Orders, inventory, identity and payment records benefit from strong consistency and relational modeling.

---

## ADR-004 — Redis

### Decision

Use Redis for high-speed ephemeral state and caching.

### Primary initial use

Shopping cart.

---

## ADR-005 — Kafka

### Decision

Use Kafka for asynchronous integration/domain events.

### Reason

Useful for:

* Decoupling
* Fan-out
* Asynchronous processing
* Event-driven workflows

Kafka will be introduced when the business problem justifies it.

---

## ADR-006 — Vault

### Decision

Use HashiCorp Vault for credential/secrets management.

### Reason

Secrets should not be stored in Git or embedded directly in application configuration.

---

# 36. Development Environment

Current baseline:

```text
Java        21.0.4 LTS
Git         2.45.2
Docker      28.0.4
Compose     2.34.0
Node        20.15.1 currently installed
```

Node will be upgraded to Node 24 LTS.

Maven will NOT be installed globally.

Spring Boot projects will use Maven Wrapper:

```text
mvnw
mvnw.cmd
```

On Windows:

```powershell
.\mvnw.cmd clean test
```

This keeps the Maven version controlled by the project.

---

# 37. Repository Structure

Target initial repository:

```text
shopsphere/
│
├── apps/
│
├── infrastructure/
│
├── docs/
│
├── scripts/
│
├── .github/
│
├── README.md
├── PROJECT_CONTEXT.md
├── .gitignore
└── .editorconfig
```

Later:

```text
apps/
├── api-gateway/
├── identity-service/
├── product-service/
├── cart-service/
├── order-service/
├── inventory-service/
├── payment-service/
├── notification-service/
└── frontend/
```

---

# 38. Development Sequence

We will build incrementally.

## Phase 1 — Foundation

* Jira
* GitHub
* Documentation
* Development environment
* Docker Compose
* CI foundation

## Phase 2 — Product Service

* Product domain
* REST API
* PostgreSQL
* Flyway
* Validation
* Exception handling
* OpenAPI
* Unit tests
* Integration tests
* Testcontainers

## Phase 3 — Identity

* Registration
* Password hashing
* Authentication
* Authorization
* Vault

## Phase 4 — Cart

* React cart
* Redis
* TTL
* Concurrency

## Phase 5 — Orders

* Checkout
* Order state machine
* Idempotency
* Persistence

## Phase 6 — Inventory & Payment

* Inventory reservation
* Payment abstraction
* Failure handling
* Idempotency

## Phase 7 — Kafka

* Events
* Consumers
* Consumer groups
* Retry
* Dead-letter topics
* Outbox pattern
* Schema evolution

## Phase 8 — Frontend

* React application
* Authentication
* Product catalog
* Cart
* Checkout
* Orders
* Admin

## Phase 9 — Observability

* Structured logging
* Metrics
* Tracing
* Prometheus
* Grafana
* OpenTelemetry

## Phase 10 — CI/CD

* GitHub Actions
* Container registry
* Security scanning
* Automated deployment

## Phase 11 — Kubernetes

* Deployments
* Services
* Ingress
* Secrets
* ConfigMaps
* HPA
* Health probes

## Phase 12 — Production Hardening

* Load testing
* Rate limiting
* Resilience
* Disaster recovery
* Security hardening
* Failure testing

---

# 39. End-to-End Engineering Workflow

Every meaningful feature should follow:

```text
Business Requirement
        ↓
BRD / Requirement
        ↓
Jira Epic
        ↓
Jira Story
        ↓
Acceptance Criteria
        ↓
Architecture / Design
        ↓
ADR if required
        ↓
Technical Tasks
        ↓
Git Branch
        ↓
Implementation
        ↓
Unit Tests
        ↓
Integration Tests
        ↓
Pull Request
        ↓
Code Review
        ↓
CI
        ↓
Docker Image
        ↓
Deployment
        ↓
Smoke Tests
        ↓
Observability
        ↓
Jira Done
```

---

# 40. Important Rule: Learn the Why

For every major implementation, we will discuss:

1. What are we building?
2. Why are we building it?
3. What problem does it solve?
4. What alternatives exist?
5. Why did we choose this solution?
6. What are the trade-offs?
7. How does it scale?
8. How does it fail?
9. How do we test it?
10. How do we observe it?
11. How do we deploy it?
12. How do we roll it back?

The goal is not to memorize Spring Boot annotations or Kubernetes YAML.

The goal is to learn **software engineering and system design**.

---

# 41. Immediate Next Steps

The current state is:

```text
Jira
  ✅ Created

GitHub repository
  ✅ Created

Java 21
  ✅ Installed

Git
  ✅ Installed

Docker
  ✅ Installed

Docker Compose
  ✅ Installed

Maven
  ✅ Will use Maven Wrapper

Node.js
  ⚠️ Upgrade from 20 → 24 LTS

Repository documentation
  ⏳ This document
```

After committing this file, our next sequence is:

```text
1. Upgrade Node.js
       ↓
2. Create repository structure
       ↓
3. Create .gitignore
       ↓
4. Create .editorconfig
       ↓
5. Improve README
       ↓
6. Commit SHOP-1 / SHOP-2
       ↓
7. Create Docker Compose
       ↓
8. Start PostgreSQL
       ↓
9. Start Redis
       ↓
10. Start Kafka
       ↓
11. Create Spring Boot Product Service
```

---

# 42. Current Project Status

We are currently at:

**Sprint 1 — Engineering Foundation**

Current immediate objective:

> Establish a clean, reproducible development environment and repository before writing application code.

The first application service will be **Product Service**.

We deliberately start with Product Service rather than Identity or Order because it allows us to learn:

* Spring Boot
* REST
* Domain modeling
* PostgreSQL
* Flyway
* Validation
* Error handling
* Testing
* Docker
* CI

without introducing authentication, distributed transactions and Kafka complexity immediately.

---

# 43. Guiding Principle

> Build a simple thing correctly before building a complex thing.

We will progressively evolve:

```text
Simple REST Service
        ↓
Database
        ↓
Tests
        ↓
Docker
        ↓
Multiple Services
        ↓
Redis
        ↓
Kafka
        ↓
Security
        ↓
Observability
        ↓
CI/CD
        ↓
Kubernetes
        ↓
Scalability
        ↓
Production Hardening
```

This is intentional.

We want to understand the reason each layer exists before adding the next one.

---

# End of Project Context

````

After you add it to GitHub, commit it as:

```bash
git add PROJECT_CONTEXT.md
git commit -m "docs: add project context and engineering plan"
git push origin main
````

Then we can treat `PROJECT_CONTEXT.md` as our **persistent project memory** and continue from it as the source of truth.
