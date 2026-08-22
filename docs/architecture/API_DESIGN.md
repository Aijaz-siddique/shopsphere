Absolutely. Below is the \*\*complete revised `API\_DESIGN.md`\*\*, incorporating the review changes. Replace the existing file with this entire content.



````markdown

\# ShopSphere

\## Product Service API Design



\*\*Document Version:\*\* 1.1  

\*\*Status:\*\* Draft  

\*\*Related Documents:\*\*

\- `docs/product-service/BRD.md`

\- `docs/product-service/FRD.md`

\- `docs/product-service/DOMAIN\_MODEL.md`

\- `docs/architecture/HLD.md`



\*\*Last Updated:\*\* 2026-08-23



\---



\# 1. Purpose



This document defines the REST API contract for the ShopSphere Product Service.



It defines:



\- API endpoints

\- HTTP methods

\- Request structures

\- Response structures

\- Validation rules

\- HTTP status codes

\- Error responses

\- Pagination

\- Filtering

\- Sorting

\- API versioning

\- Idempotency

\- Correlation IDs

\- Authentication expectations



This document defines the external API contract.



Internal implementation details such as controllers, services, repositories, and domain classes belong in the Low-Level Design.



\---



\# 2. API Design Principles



The Product Service API follows these principles:



1\. RESTful resource-oriented APIs

2\. Explicit API versioning

3\. JSON request and response bodies

4\. Consistent HTTP status codes

5\. Consistent error responses

6\. Validation at the API boundary

7\. Pagination for collection endpoints

8\. No database models exposed directly

9\. Stable API contracts

10\. Backward compatibility where practical

11\. Correlation IDs for traceability

12\. Idempotency for retry-sensitive operations where required

13\. API contracts remain independent of persistence implementation



\---



\# 3. Base URL



The external API will be exposed through the API Gateway.



The logical base path is:



```text

/api/v1

````



Product endpoints therefore use:



```text

/api/v1/products

```



The actual hostname will depend on the deployment environment.



Examples:



```text

Development:

http://localhost:8080/api/v1/products



Production:

https://<domain>/api/v1/products

```



The production hostname is intentionally not defined yet.



\---



\# 4. Resource Model



The primary API resource is:



```text

Product

```



A Product has the following conceptual representation:



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "description": "Apple iPhone 15 with 128GB storage",

&#x20; "price": 799.99,

&#x20; "currency": "USD",

&#x20; "categoryId": "01JCATEGORY001",

&#x20; "brandId": "01JBRAND001",

&#x20; "status": "ACTIVE",

&#x20; "createdAt": "2026-08-23T10:30:00Z",

&#x20; "updatedAt": "2026-08-23T10:30:00Z"

}

```



The API representation is intentionally separate from the database representation.



\---



\# 5. Product Lifecycle



Products can have the following lifecycle states:



```text

DRAFT

ACTIVE

INACTIVE

ARCHIVED

```



Conceptual lifecycle:



```text

DRAFT

&#x20; │

&#x20; │ activate

&#x20; ▼

ACTIVE

&#x20; │

&#x20; │ deactivate

&#x20; ▼

INACTIVE

&#x20; │

&#x20; │ archive

&#x20; ▼

ARCHIVED

```



Not every transition will necessarily be allowed.



Business rules for lifecycle transitions belong to the domain/application layer.



\---



\# 6. Create Product



\## Endpoint



```http

POST /api/v1/products

```



Creates a new product.



A newly created Product initially enters the `DRAFT` state.



\---



\## Request Headers



Required:



```http

Content-Type: application/json

Accept: application/json

X-Correlation-ID: <correlation-id>

```



Authentication will eventually be required for this operation.



\---



\## Request Body



```json

{

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "description": "Apple iPhone 15 with 128GB storage",

&#x20; "price": 799.99,

&#x20; "currency": "USD",

&#x20; "categoryId": "01JCATEGORY001",

&#x20; "brandId": "01JBRAND001"

}

```



\---



\## Request Fields



| Field       | Type    | Required | Description         |

| ----------- | ------- | -------: | ------------------- |

| sku         | string  |      Yes | Unique product SKU  |

| name        | string  |      Yes | Product name        |

| description | string  |       No | Product description |

| price       | decimal |      Yes | Product price       |

| currency    | string  |      Yes | ISO currency code   |

| categoryId  | string  |       No | Category identifier |

| brandId     | string  |       No | Brand identifier    |



\---



\## Validation



\### SKU



\* Required

\* Must be unique

\* Must not be blank

\* Maximum length will be defined in domain constraints



\### Name



\* Required

\* Must not be blank



\### Price



\* Required

\* Must be greater than or equal to zero

\* Monetary precision must be controlled



\### Currency



\* Required

\* Must be a valid supported currency code



\---



\## Success Response



\### HTTP 201 Created



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "description": "Apple iPhone 15 with 128GB storage",

&#x20; "price": 799.99,

&#x20; "currency": "USD",

&#x20; "categoryId": "01JCATEGORY001",

&#x20; "brandId": "01JBRAND001",

&#x20; "status": "DRAFT",

&#x20; "createdAt": "2026-08-23T10:30:00Z",

&#x20; "updatedAt": "2026-08-23T10:30:00Z"

}

```



\---



\## Response Headers



The response should include:



```http

Location: /api/v1/products/01JABC123XYZ

X-Correlation-ID: <correlation-id>

```



\---



\# 7. Get Product



\## Endpoint



```http

GET /api/v1/products/{productId}

```



Returns a single product.



\---



\## Example



```http

GET /api/v1/products/01JABC123XYZ

```



\---



\## Success Response



\### HTTP 200 OK



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "description": "Apple iPhone 15 with 128GB storage",

&#x20; "price": 799.99,

&#x20; "currency": "USD",

&#x20; "categoryId": "01JCATEGORY001",

&#x20; "brandId": "01JBRAND001",

&#x20; "status": "ACTIVE",

&#x20; "createdAt": "2026-08-23T10:30:00Z",

&#x20; "updatedAt": "2026-08-23T10:30:00Z"

}

```



\---



\## Product Not Found



\### HTTP 404 Not Found



```json

{

&#x20; "code": "PRODUCT\_NOT\_FOUND",

&#x20; "message": "Product was not found",

&#x20; "correlationId": "abc-123",

&#x20; "timestamp": "2026-08-23T10:35:00Z"

}

```



\---



\# 8. List Products



\## Endpoint



```http

GET /api/v1/products

```



Returns a paginated collection of products.



\---



\# 9. Pagination



Pagination will use page-based pagination initially.



Example:



```http

GET /api/v1/products?page=0\&size=20

```



Parameters:



| Parameter | Default | Description                |

| --------- | ------: | -------------------------- |

| page      |       0 | Zero-based page number     |

| size      |      20 | Number of records per page |



The maximum page size should be restricted.



Initial proposed maximum:



```text

100

```



\---



\# 10. List Response



\### HTTP 200 OK



```json

{

&#x20; "content": \[

&#x20;   {

&#x20;     "id": "01JABC123XYZ",

&#x20;     "sku": "IPHONE-15-128-BLK",

&#x20;     "name": "iPhone 15",

&#x20;     "description": "Apple iPhone 15 with 128GB storage",

&#x20;     "price": 799.99,

&#x20;     "currency": "USD",

&#x20;     "categoryId": "01JCATEGORY001",

&#x20;     "brandId": "01JBRAND001",

&#x20;     "status": "ACTIVE",

&#x20;     "createdAt": "2026-08-23T10:30:00Z",

&#x20;     "updatedAt": "2026-08-23T10:30:00Z"

&#x20;   }

&#x20; ],

&#x20; "page": 0,

&#x20; "size": 20,

&#x20; "totalElements": 1,

&#x20; "totalPages": 1

}

```



\---



\# 11. Product Filtering



The Product API will support filtering.



Initial filters:



```text

status

categoryId

brandId

sku

```



Example:



```http

GET /api/v1/products?status=ACTIVE

```



Multiple filters may be combined.



Example:



```http

GET /api/v1/products?status=ACTIVE\&categoryId=01JCATEGORY001

```



Only explicitly supported filters will be exposed.



\---



\# 12. Product Search



The initial API may support simple name-based search.



Example:



```http

GET /api/v1/products?search=iphone

```



Initial search is an application-level search capability.



A dedicated search engine such as Elasticsearch/OpenSearch will not be introduced unless functional, scalability, or performance requirements justify it.



Search semantics may be expanded later without changing the core Product resource model.



\---



\# 13. Sorting



The collection endpoint will support sorting.



Example:



```http

GET /api/v1/products?sort=name,asc

```



or:



```http

GET /api/v1/products?sort=price,desc

```



Only approved sortable fields should be exposed.



Initial candidates:



```text

name

price

createdAt

updatedAt

```



The service must not allow arbitrary database column names to be passed as sort parameters.



\---



\# 14. Update Product



\## Endpoint



```http

PUT /api/v1/products/{productId}

```



Updates a product.



The initial implementation treats `PUT` as a complete update of the mutable Product representation.



Clients should provide all mutable Product fields.



\---



\## Example



```http

PUT /api/v1/products/01JABC123XYZ

```



\---



\## Request



```json

{

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "description": "Updated product description",

&#x20; "price": 779.99,

&#x20; "currency": "USD",

&#x20; "categoryId": "01JCATEGORY001",

&#x20; "brandId": "01JBRAND001"

}

```



\---



\## Success Response



\### HTTP 200 OK



Returns the updated Product representation.



\---



\## Product Not Found



\### HTTP 404 Not Found



Returns the standard `PRODUCT\_NOT\_FOUND` error response.



\---



\## Concurrent Update Protection



Optimistic concurrency may be introduced to prevent stale clients from overwriting newer changes.



A future implementation may use:



```http

If-Match: "<etag>"

```



This is intentionally deferred until concurrent update requirements are established.



\---



\# 15. Activate Product



\## Endpoint



```http

POST /api/v1/products/{productId}/activate

```



Transitions a Product from an activatable lifecycle state to `ACTIVE`.



Typical initial transition:



```text

DRAFT → ACTIVE

```



\---



\## Success Response



\### HTTP 200 OK



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "status": "ACTIVE",

&#x20; "updatedAt": "2026-08-23T11:00:00Z"

}

```



\---



\## Invalid State Transition



\### HTTP 409 Conflict



```json

{

&#x20; "code": "INVALID\_PRODUCT\_STATE",

&#x20; "message": "Product cannot be activated from its current state",

&#x20; "correlationId": "abc-123",

&#x20; "timestamp": "2026-08-23T11:00:00Z"

}

```



Exact lifecycle transition rules belong to the domain model.



\---



\# 16. Deactivate Product



\## Endpoint



```http

POST /api/v1/products/{productId}/deactivate

```



Transitions an active Product to `INACTIVE`.



Typical transition:



```text

ACTIVE → INACTIVE

```



\---



\## Success Response



\### HTTP 200 OK



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "status": "INACTIVE",

&#x20; "updatedAt": "2026-08-23T11:05:00Z"

}

```



\---



\# 17. Archive Product



\## Endpoint



```http

POST /api/v1/products/{productId}/archive

```



Transitions a Product to `ARCHIVED`.



Typical transition:



```text

INACTIVE → ARCHIVED

```



The operation must obey valid domain lifecycle transitions.



\---



\## Success Response



\### HTTP 200 OK



```json

{

&#x20; "id": "01JABC123XYZ",

&#x20; "status": "ARCHIVED",

&#x20; "updatedAt": "2026-08-23T11:10:00Z"

}

```



\---



\# 18. Delete Product



A physical delete endpoint will not be provided initially.



There is no:



```http

DELETE /api/v1/products/{productId}

```



in the first API version.



Reason:



Products may be referenced by historical business transactions.



Lifecycle state changes are safer than physical deletion.



\---



\# 19. HTTP Status Codes



The Product API will use standard HTTP status codes.



| Status | Meaning                                       |

| -----: | --------------------------------------------- |

|    200 | Successful request                            |

|    201 | Resource created                              |

|    204 | Successful request with no response body      |

|    400 | Invalid or malformed request                  |

|    401 | Authentication required or failed             |

|    403 | Insufficient permission                       |

|    404 | Resource not found                            |

|    409 | Business conflict or invalid state transition |

|    429 | Rate limit exceeded                           |

|    500 | Unexpected server error                       |

|    503 | Service temporarily unavailable               |



Not every endpoint will use every status code.



HTTP 422 is intentionally not part of the initial API contract.



\---



\# 20. Validation Error



Invalid input should return a consistent error structure.



Example:



\### HTTP 400 Bad Request



```json

{

&#x20; "code": "VALIDATION\_ERROR",

&#x20; "message": "Request validation failed",

&#x20; "correlationId": "abc-123",

&#x20; "timestamp": "2026-08-23T11:05:00Z",

&#x20; "errors": \[

&#x20;   {

&#x20;     "field": "name",

&#x20;     "message": "Name must not be blank"

&#x20;   },

&#x20;   {

&#x20;     "field": "price",

&#x20;     "message": "Price must be greater than or equal to zero"

&#x20;   }

&#x20; ]

}

```



\---



\# 21. Duplicate SKU



SKU uniqueness is a business rule.



If a client attempts to create a product using an existing SKU:



\### HTTP 409 Conflict



```json

{

&#x20; "code": "SKU\_ALREADY\_EXISTS",

&#x20; "message": "A product with this SKU already exists",

&#x20; "correlationId": "abc-123",

&#x20; "timestamp": "2026-08-23T11:10:00Z"

}

```



The database must also enforce SKU uniqueness.



\---



\# 22. General Error Contract



All Product Service errors should follow a common structure.



```json

{

&#x20; "code": "ERROR\_CODE",

&#x20; "message": "Human-readable error message",

&#x20; "correlationId": "abc-123",

&#x20; "timestamp": "2026-08-23T11:10:00Z"

}

```



Optional validation details:



```json

"errors": \[

&#x20; {

&#x20;   "field": "fieldName",

&#x20;   "message": "Validation message"

&#x20; }

]

```



\---



\# 23. Error Code Principles



Error codes should be:



\* Stable

\* Machine-readable

\* Meaningful

\* Independent of internal Java exception names



Good:



```text

PRODUCT\_NOT\_FOUND

SKU\_ALREADY\_EXISTS

VALIDATION\_ERROR

INVALID\_PRODUCT\_STATE

```



Bad:



```text

NullPointerException

ProductServiceException

JpaSystemException

```



Internal implementation exceptions must never become the public API contract.



\---



\# 24. Correlation ID



Every request should have a correlation ID.



Request:



```http

X-Correlation-ID: abc-123

```



If the client does not provide one, the API Gateway or Product Service should generate one.



The ID should appear in:



\* Response headers

\* Logs

\* Error responses

\* Downstream requests



Example:



```http

X-Correlation-ID: abc-123

```



The same identifier should be propagated where appropriate across downstream service calls and asynchronous workflows.



\---



\# 25. Authentication



Authentication will eventually be enforced for protected Product operations.



Initial conceptual model:



```text

Client

&#x20; │

&#x20; ▼

API Gateway

&#x20; │

&#x20; ▼

Authentication

&#x20; │

&#x20; ▼

Product Service

```



Public product browsing may eventually be allowed without authentication.



Administrative operations such as create, update, activate, deactivate, and archive will require appropriate authorization.



Exact authentication implementation is deferred to Security Design.



\---



\# 26. Authorization



Initial conceptual permissions:



| Operation          | Expected Permission |

| ------------------ | ------------------- |

| List Products      | PRODUCT\_READ        |

| Get Product        | PRODUCT\_READ        |

| Create Product     | PRODUCT\_WRITE       |

| Update Product     | PRODUCT\_WRITE       |

| Activate Product   | PRODUCT\_WRITE       |

| Deactivate Product | PRODUCT\_WRITE       |

| Archive Product    | PRODUCT\_WRITE       |



The exact role-to-permission mapping will be finalized during authentication and authorization design.



\---



\# 27. Idempotency



Create operations can potentially be retried.



For example:



```http

POST /api/v1/products

Idempotency-Key: 12345

```



The initial API design supports the concept of idempotency for retry-sensitive operations.



The `Idempotency-Key` header is not mandatory for every POST operation in the initial implementation.



The exact storage, scope, and expiry mechanism will be defined in the Low-Level Design.



SKU uniqueness provides an additional business-level protection against duplicate Products, but it is not a complete replacement for API idempotency.



\---



\# 28. API Caching



GET operations may eventually support HTTP caching headers.



Potential headers:



```http

ETag: "<version>"

Cache-Control: ...

```



Redis caching is an internal implementation concern and should not automatically determine the external HTTP caching contract.



Caching will only be introduced when justified by performance or scalability requirements.



\---



\# 29. Request Timeout



API requests must have bounded execution time.



Timeout values will be determined during implementation and performance testing.



Long-running operations should not block synchronous HTTP requests unnecessarily.



Such operations may eventually be converted to asynchronous workflows.



\---



\# 30. API Security



All production APIs must use HTTPS.



Sensitive information must not be logged.



Examples of information that must not appear in logs:



\* Passwords

\* Authentication tokens

\* API secrets

\* Payment credentials

\* Sensitive personal information



\---



\# 31. API Observability



Each request should provide sufficient information for troubleshooting.



At minimum:



```text

HTTP method

Path

Status code

Latency

Correlation ID

Authenticated principal where applicable

```



Logs must avoid sensitive information.



\---



\# 32. API Compatibility



Once an API version is released, breaking changes should not be introduced casually.



Breaking changes should generally result in a new major API version.



Example:



```text

/api/v1/products

/api/v2/products

```



Non-breaking additions should preferably be introduced without creating unnecessary versions.



\---



\# 33. API Documentation



The Product Service API will eventually be documented using OpenAPI/Swagger.



Expected endpoint documentation:



```text

POST   /api/v1/products

GET    /api/v1/products

GET    /api/v1/products/{id}

PUT    /api/v1/products/{id}

POST   /api/v1/products/{id}/activate

POST   /api/v1/products/{id}/deactivate

POST   /api/v1/products/{id}/archive

```



OpenAPI will become the executable API contract where practical.



\---



\# 34. Initial API Summary



| Method | Endpoint                           | Purpose                         |

| ------ | ---------------------------------- | ------------------------------- |

| POST   | `/api/v1/products`                 | Create Product                  |

| GET    | `/api/v1/products`                 | List / search / filter Products |

| GET    | `/api/v1/products/{id}`            | Get Product                     |

| PUT    | `/api/v1/products/{id}`            | Complete update of Product      |

| POST   | `/api/v1/products/{id}/activate`   | Activate Product                |

| POST   | `/api/v1/products/{id}/deactivate` | Deactivate Product              |

| POST   | `/api/v1/products/{id}/archive`    | Archive Product                 |



No physical DELETE endpoint initially.



\---



\# 35. Example API Flow



Create Product:



```text

Client

&#x20;  │

&#x20;  │ POST /api/v1/products

&#x20;  ▼

API Gateway

&#x20;  │

&#x20;  │ Authentication / Authorization

&#x20;  ▼

Product Service

&#x20;  │

&#x20;  ├── Validate Request

&#x20;  │

&#x20;  ├── Execute Business Rules

&#x20;  │

&#x20;  ├── Persist Product

&#x20;  │

&#x20;  └── Write Outbox Event

&#x20;           │

&#x20;           ▼

&#x20;       PostgreSQL

```



After transaction commit:



```text

Outbox Relay

&#x20;    │

&#x20;    ▼

&#x20;  Kafka

```



The Product database transaction and event publication must preserve the consistency guarantees defined by the HLD.



\---



\# 36. API-to-Domain Alignment



The API must not expose domain implementation details.



For example, the API should expose:



```json

{

&#x20; "sku": "IPHONE-15-128-BLK",

&#x20; "name": "iPhone 15",

&#x20; "price": 799.99

}

```



rather than exposing:



```text

ProductEntity

JPA annotations

Database primary keys

Internal persistence fields

```



The API contract represents the business capability, not the database schema.



\---



\# 37. API-to-Database Separation



The API should not be designed directly from database tables.



The intended flow is:



```text

API Contract

&#x20;    │

&#x20;    ▼

Application Model

&#x20;    │

&#x20;    ▼

Domain Model

&#x20;    │

&#x20;    ▼

Persistence Model

&#x20;    │

&#x20;    ▼

Database

```



Changes to database implementation should not automatically become breaking API changes.



\---



\# 38. Future API Considerations



The following are intentionally deferred:



\* Bulk Product operations

\* Product images

\* Product variants

\* Product attributes

\* Advanced search

\* Full-text search

\* Faceted search

\* Product recommendations

\* Category management APIs

\* Brand management APIs

\* Import/export APIs

\* Webhooks

\* GraphQL



These will be introduced only when required.



\---



\# 39. Non-Goals



The initial Product API will not attempt to solve:



\* Inventory management

\* Order management

\* Payment processing

\* Customer management

\* Search engine integration

\* Recommendation engine

\* Product reviews

\* Product analytics



Those belong to other capabilities.



\---



\# 40. API Design Status



\*\*Status:\*\* Draft



This document defines the initial Product Service API contract.



It will be reviewed against:



\* BRD

\* FRD

\* Domain Model

\* HLD



before implementation begins.



\---



\# 41. Next Steps



After API design approval:



1\. Review API contract

2\. Create `DATABASE\_DESIGN.md`

3\. Define Product database schema

4\. Define indexes and constraints

5\. Define database migrations

6\. Create Product Service LLD

7\. Implement Spring Boot Product Service

8\. Add automated tests

9\. Integrate PostgreSQL

10\. Add API documentation



```



\### One small note



I intentionally kept the document at \*\*API-contract level\*\*. We are \*not\* yet specifying things like:



\- Spring controller classes

\- DTO class names

\- JPA entities

\- repository methods

\- exact database columns

\- Kafka topic implementation

\- JWT implementation



Those belong in the next design stages.



Once you've replaced the file, \*\*don't commit yet\*\*. We'll do one final review of this revised version, then commit it.

```



