

\## 1. Create the FRD



Create:



```text

docs/product-service/FRD.md

```



We'll define:



```text

BRD

&#x20;↓

FRD

&#x20;↓

Use Cases

&#x20;↓

API Contract

&#x20;↓

HLD

&#x20;↓

LLD

&#x20;↓

Code

```



\### The first few use cases will be:



| ID     | Use Case           |

| ------ | ------------------ |

| UC-001 | Create Product     |

| UC-002 | Get Product        |

| UC-003 | List Products      |

| UC-004 | Search Products    |

| UC-005 | Update Product     |

| UC-006 | Activate Product   |

| UC-007 | Deactivate Product |

| UC-008 | Archive Product    |



\---



\# 2. FRD — Version 1



Put this in `FRD.md`:



````markdown

\# ShopSphere Product Service

\## Functional Requirements Document (FRD)



\*\*Document Version:\*\* 1.0  

\*\*Status:\*\* Draft  

\*\*Related BRD:\*\* `docs/product-service/BRD.md`



\---



\# 1. Purpose



This document defines the functional behavior of the ShopSphere Product Service.



The Product Service is responsible for managing the product catalog and exposing product capabilities to customers and authorized internal users.



\---



\# 2. Actors



\## Customer



Can:



\- View active products

\- Search active products

\- Filter active products

\- View product details



\## Product Manager



Can:



\- Create products

\- Update products

\- Activate products

\- Deactivate products

\- Archive products

\- View products



\## Administrator



Can perform all Product Manager operations.



\---



\# 3. Product States



The Product Service supports:



```text

DRAFT

ACTIVE

INACTIVE

ARCHIVED

````



Valid transitions:



```text

&#x20;            ┌──────────┐

&#x20;            │  DRAFT   │

&#x20;            └────┬─────┘

&#x20;                 │ activate

&#x20;                 ▼

&#x20;            ┌──────────┐

&#x20;            │  ACTIVE  │

&#x20;            └────┬─────┘

&#x20;                 │ deactivate

&#x20;                 ▼

&#x20;            ┌──────────┐

&#x20;            │ INACTIVE │

&#x20;            └────┬─────┘

&#x20;                 │ archive

&#x20;                 ▼

&#x20;            ┌──────────┐

&#x20;            │ ARCHIVED │

&#x20;            └──────────┘

```



An active product may also be deactivated.



A product in `ARCHIVED` state cannot return to another state.



\---



\# 4. UC-001 Create Product



\## Actor



Product Manager / Administrator



\## Description



Creates a new product in the catalog.



\## Preconditions



\* User is authenticated.

\* User has product management permission.



\## Main Flow



1\. User submits product information.

2\. System validates the request.

3\. System validates SKU uniqueness.

4\. System validates price.

5\. System validates currency.

6\. System creates the product.

7\. Product status is set to `DRAFT`.

8\. System stores the product.

9\. System returns the created product.



\## Required Fields



\* SKU

\* Name

\* Description

\* Category

\* Brand

\* Price

\* Currency



\## Failure Scenarios



\### Duplicate SKU



Return a conflict response.



\### Invalid price



Return a validation error.



\### Invalid currency



Return a validation error.



\---



\# 5. UC-002 Get Product



\## Actor



Customer / Product Manager / Administrator



\## Description



Retrieves a product using its unique ID.



\## Main Flow



1\. User sends product ID.

2\. System validates the ID.

3\. System retrieves the product.

4\. System verifies visibility rules.

5\. System returns the product.



\## Rules



Customers can retrieve only `ACTIVE` products.



Product Managers and Administrators may retrieve products in any state.



\## Failure Scenarios



\### Product does not exist



Return not-found response.



\### Customer requests inactive product



Return not-found response.



\---



\# 6. UC-003 List Products



\## Actor



Customer / Product Manager / Administrator



\## Description



Returns a paginated list of products.



\## Supported Parameters



```text

page

size

sort

status

category

brand

```



\## Customer Behavior



Customers receive only active products.



\## Administrative Behavior



Authorized internal users may request products in any state.



\## Pagination



The API must use server-side pagination.



Example:



```text

GET /api/v1/products?page=0\&size=20

```



\---



\# 7. UC-004 Search Products



\## Actor



Customer / Product Manager / Administrator



\## Description



Searches for products matching supplied criteria.



\## Initial Search Fields



\* Name

\* SKU

\* Category

\* Brand



\## Customer Behavior



Only active products are searchable by customers.



\## Future Enhancement



Advanced product search may be implemented using a dedicated search engine.



\---



\# 8. UC-005 Update Product



\## Actor



Product Manager / Administrator



\## Description



Updates product information.



\## Main Flow



1\. User sends product ID.

2\. System retrieves the product.

3\. System validates the request.

4\. System checks version.

5\. System updates the product.

6\. System increments the version.

7\. System stores the changes.

8\. System returns the updated product.



\## Concurrency



The system shall use optimistic locking to prevent lost updates.



If the supplied version is stale, the system shall reject the update.



\---



\# 9. UC-006 Activate Product



\## Actor



Product Manager / Administrator



\## Description



Activates a product.



\## Preconditions



The product must contain all mandatory information.



\## Valid Transition



```text

DRAFT → ACTIVE

```



or:



```text

INACTIVE → ACTIVE

```



\## Main Flow



1\. User requests activation.

2\. System retrieves the product.

3\. System validates product completeness.

4\. System validates current state.

5\. System changes status to `ACTIVE`.

6\. System persists the change.

7\. System publishes a product activation event.



\---



\# 10. UC-007 Deactivate Product



\## Actor



Product Manager / Administrator



\## Description



Temporarily removes a product from customer visibility.



\## Valid Transition



```text

ACTIVE → INACTIVE

```



\## Main Flow



1\. User requests deactivation.

2\. System retrieves the product.

3\. System validates current state.

4\. System changes status.

5\. System persists the change.

6\. System publishes a product deactivation event.



\---



\# 11. UC-008 Archive Product



\## Actor



Product Manager / Administrator



\## Description



Permanently removes a product from normal catalog operations.



\## Valid Transition



```text

INACTIVE → ARCHIVED

```



\## Rules



An archived product:



\* Cannot be purchased.

\* Cannot appear in customer listings.

\* Cannot be activated again.

\* Must remain stored for audit/history purposes.



\---



\# 12. API Behavior



\## Create



```http

POST /api/v1/products

```



\## Get



```http

GET /api/v1/products/{id}

```



\## List



```http

GET /api/v1/products

```



\## Update



```http

PUT /api/v1/products/{id}

```



\## Activate



```http

PATCH /api/v1/products/{id}/activate

```



\## Deactivate



```http

PATCH /api/v1/products/{id}/deactivate

```



\## Archive



```http

PATCH /api/v1/products/{id}/archive

```



\---



\# 13. Validation



The system must validate:



\## SKU



\* Required

\* Unique

\* Maximum length to be defined during API design



\## Name



\* Required

\* Must not be blank



\## Description



\* Required

\* Must not be blank



\## Price



\* Must be greater than or equal to zero



\## Currency



\* Required

\* Must be a supported ISO currency code



\## Category



\* Required



\## Brand



\* Required



\---



\# 14. Error Handling



The API must return consistent error responses.



Example:



```json

{

&#x20; "code": "PRODUCT\_NOT\_FOUND",

&#x20; "message": "Product was not found",

&#x20; "timestamp": "2026-08-22T18:00:00Z",

&#x20; "path": "/api/v1/products/123"

}

```



Detailed error contract will be defined during API design.



\---



\# 15. Idempotency



The system should prevent accidental duplicate operations.



For create operations, an idempotency mechanism may be introduced.



For state transitions, repeating an already completed operation should return a predictable response rather than producing duplicate state changes.



\---



\# 16. Concurrency



The Product Service must support concurrent updates.



Optimistic locking will be used.



Conceptually:



```text

Product

\--------

id

version

...

```



An update must include the expected version.



If the database version differs, the update must fail.



\---



\# 17. Events



The Product Service will eventually publish events:



```text

ProductCreated

ProductUpdated

ProductActivated

ProductDeactivated

ProductArchived

```



Events will be delivered through Kafka.



The exact event schemas will be defined during the event/API design phase.



\---



\# 18. Auditability



The system should retain:



\* Created timestamp

\* Updated timestamp

\* Version



A more complete audit history may be introduced later.



\---



\# 19. Security



Authentication and authorization will eventually be handled through the ShopSphere identity architecture.



The Product Service must enforce authorization at the application boundary.



Example:



```text

Customer

&#x20;  |

&#x20;  └── READ active products



Product Manager

&#x20;  |

&#x20;  └── READ + WRITE products



Administrator

&#x20;  |

&#x20;  └── FULL ACCESS

```



\---



\# 20. Performance



Initial targets:



| Operation     | Target       |

| ------------- | ------------ |

| Get product   | P95 < 300 ms |

| List products | P95 < 500 ms |

| Search        | P95 < 500 ms |

| Create/update | P95 < 500 ms |



These targets will be validated using performance testing.



\---



\# 21. Availability



The Product Service must be stateless at the application layer.



Multiple instances must be able to run concurrently.



```text

&#x20;             Load Balancer

&#x20;                   |

&#x20;         ┌─────────┼─────────┐

&#x20;         ▼         ▼         ▼

&#x20;      Instance  Instance  Instance

&#x20;         │         │         │

&#x20;         └─────────┼─────────┘

&#x20;                   ▼

&#x20;              PostgreSQL

```



\---



\# 22. Observability



The service must provide:



\* Health endpoint

\* Application logs

\* Metrics

\* Distributed tracing



The exact observability stack will be selected during HLD.



\---



\# 23. Out of Scope



The following are not part of the initial Product Service implementation:



\* Payments

\* Orders

\* Inventory

\* Shopping cart

\* Reviews

\* Recommendations

\* Product images

\* Advanced search infrastructure

\* Customer authentication implementation



\---



\# 24. Acceptance Criteria



The Product Service implementation is functionally acceptable when:



1\. Authorized users can create products.

2\. Duplicate SKUs are rejected.

3\. Products start in `DRAFT`.

4\. Customers can retrieve active products.

5\. Customers cannot retrieve inactive products.

6\. Products can be updated.

7\. Concurrent updates are protected.

8\. Products can transition through valid lifecycle states.

9\. Invalid lifecycle transitions are rejected.

10\. Archived products cannot be reactivated.

11\. APIs provide consistent errors.

12\. Product changes can generate events.

13\. Product data remains persistent across application restarts.

14\. Critical behavior is covered by automated tests.



````



\---



\# 3. Something important we just introduced



There are several concepts here that we'll deliberately explore rather than just implement mechanically:



\### Optimistic locking



We'll eventually have:



```text

Product

&#x20;├── id

&#x20;├── ...

&#x20;└── version

````



This will teach you how distributed/concurrent systems deal with:



```text

User A ──┐

&#x20;        ├── Update Product

User B ──┘

```



without silently losing updates.



\### Idempotency



This becomes especially important when we introduce:



```text

REST

Kafka

Retries

Distributed systems

```



\### Stateless services



This is what will eventually allow:



```text

Product Service

&#x20;  ├── Pod 1

&#x20;  ├── Pod 2

&#x20;  ├── Pod 3

&#x20;  └── Pod 4

```



without requiring sticky sessions.



\---



\# 4. Don't commit yet



Save the file, but \*\*don't commit it yet\*\*.



There's one thing I want us to do next: \*\*review the FRD together and identify any business/design decisions that need to be changed before we freeze it.\*\*



For example, we've currently decided:



```text

DRAFT → ACTIVE → INACTIVE → ARCHIVED

```



But we'll question whether:



```text

DRAFT → INACTIVE

ACTIVE → DRAFT

ARCHIVED → anything

```



should be allowed.



That's exactly the sort of reasoning you should learn as part of this exercise.



\*\*Next step: we'll review the state machine and the product domain model before moving to HLD.\*\*





