Absolutely. I consolidated the review changes and did a final consistency pass against the documents we've established so far.



Below is the **complete revised `DATABASE_DESIGN.md`**. Replace the entire file with this version.



````markdown

# ShopSphere

## Product Service Database Design



**Document Version:** 1.1  

**Status:** Draft  

**Database:** PostgreSQL 17  

**Related Documents:**

- `docs/product-service/BRD.md`

- `docs/product-service/FRD.md`

- `docs/product-service/DOMAIN_MODEL.md`

- `docs/architecture/HLD.md`

- `docs/architecture/API_DESIGN.md`



**Last Updated:** 2026-08-23



---



# 1. Purpose



This document defines the PostgreSQL database design for the ShopSphere Product Service.



It covers:



- Database structure

- Tables

- Columns

- Data types

- Primary keys

- Foreign keys

- Unique constraints

- Check constraints

- Indexes

- Audit fields

- Product lifecycle persistence

- Transaction boundaries

- Database migration strategy

- Transactional outbox persistence



The database design must support the approved Product Service API and Domain Model without exposing persistence concerns to the API contract.



---



# 2. Database Principles



The Product Service database follows these principles:



1. PostgreSQL is the system of record for Product data.

2. Database constraints enforce critical persistence invariants.

3. Database schema is managed through versioned migrations.

4. Foreign keys are used only where the referenced data is owned by the Product Service.

5. Monetary values use exact numeric types rather than floating-point types.

6. Timestamps are stored consistently using timezone-aware PostgreSQL types.

7. Application/domain models are separate from persistence models.

8. Physical deletion of Products is not supported initially.

9. Indexes are introduced based on actual query requirements.

10. Database changes should be backward-compatible where practical.

11. Event publication uses the transactional outbox pattern.

12. Database credentials and secrets are never stored in source control.



---



# 3. Database Name



The initial local development database is:



```text

shopsphere

````



The actual database name may differ between environments.



Example local configuration:



```text

Database:

shopsphere



Host:

localhost



Port:

5432

```



Production database configuration will be environment-specific.



---



# 4. PostgreSQL Schema



The initial Product Service will use the PostgreSQL `public` schema.



Example:



```text

shopsphere

&#x20;   │

&#x20;   └── public

&#x20;        ├── products

&#x20;        └── outbox_events

```



A dedicated application schema may be introduced later if required by the deployment architecture.



---



# 5. Entity Overview



Initial tables:



```text

products

outbox_events

```



Conceptual relationship:



```text

┌─────────────────────┐

│      products       │

├─────────────────────┤

│ id                  │

│ sku                 │

│ name                │

│ description         │

│ price               │

│ currency            │

│ category_id         │

│ brand_id            │

│ status              │

│ created_at          │

│ updated_at          │

└──────────┬──────────┘

&#x20;          │

&#x20;          │ Product changes

&#x20;          ▼

┌─────────────────────┐

│    outbox_events    │

├─────────────────────┤

│ id                  │

│ aggregate_type      │

│ aggregate_id        │

│ event_type          │

│ payload             │

│ created_at          │

│ published_at        │

└─────────────────────┘

```



`outbox_events` does not use a database foreign key to `products` because the outbox represents events and must remain independently processable.



---



# 6. Products Table



Primary table:



```text

products

```



Purpose:



Stores the authoritative Product state.



---



# 7. Products Table Definition



Logical structure:



| Column      | PostgreSQL Type | Nullable | Constraint / Purpose       |

| ----------- | --------------- | -------: | -------------------------- |

| id          | varchar(26)     |       No | Primary key                |

| sku         | varchar(100)    |       No | Unique SKU                 |

| name        | varchar(255)    |       No | Product name               |

| description | text            |      Yes | Product description        |

| price       | numeric(19,4)   |       No | Monetary amount            |

| currency    | varchar(3)      |       No | Currency code              |

| category_id | varchar(26)     |      Yes | Logical Category reference |

| brand_id    | varchar(26)     |      Yes | Logical Brand reference    |

| status      | varchar(20)     |       No | Product lifecycle state    |

| created_at  | timestamptz     |       No | Creation timestamp         |

| updated_at  | timestamptz     |       No | Last update timestamp      |



---



# 8. Product ID



The Product ID will be represented as:



```text

varchar(26)

```



The initial implementation will use ULID-style identifiers.



Example:



```text

01JABC123XYZ...

```



Benefits:



* Globally unique

* Suitable for distributed systems

* Time-sortable

* Does not expose database sequence counts

* Can be generated independently by application instances



The application generates the Product ID.



The database does not generate Product IDs through a sequence or identity column.



The exact Java ID-generation implementation belongs in the Low-Level Design.



The database should treat the identifier as an opaque value.



---



# 9. SKU



Column:



```text

sku varchar(100) NOT NULL

```



SKU must be unique.



Database constraint:



```sql

UNIQUE (sku)

```



Example:



```text

IPHONE-15-128-BLK

```



The application should validate SKU format and uniqueness before persistence.



The database remains the final authority for uniqueness and must protect against race conditions between concurrent requests.



SKU normalization and case-sensitivity rules will be finalized during implementation.



---



# 10. Product Name



Column:



```text

name varchar(255) NOT NULL

```



Rules:



* Must not be null

* Must not be blank

* Maximum length: 255 characters



Application validation should reject blank values before persistence.



---



# 11. Description



Column:



```text

description text

```



Description is optional.



No arbitrary database length limit is imposed initially.



Application-level limits may be introduced if required by functional requirements.



---



# 12. Price



Column:



```text

price numeric(19,4) NOT NULL

```



The database must not use:



```text

float

real

double precision

```



for monetary values.



Reason:



Floating-point representations can introduce precision errors.



The application should use Java `BigDecimal`.



Example:



```text

799.9900

```



Database constraint:



```sql

CHECK (price >= 0)

```



---



# 13. Currency



Column:



```text

currency varchar(3) NOT NULL

```



The value represents a three-letter currency code.



Examples:



```text

USD

EUR

INR

GBP

```



The application is responsible for validating supported currencies.



The database does not maintain a hard-coded list of supported currencies initially.



This avoids requiring a schema migration whenever the supported currency set changes.



---



# 14. Category Reference



Column:



```text

category_id varchar(26)

```



The Product Service may reference a Category.



The initial design does not create a Category table inside the Product Service unless Category ownership is explicitly assigned to this service.



Therefore:



```text

category_id

```



is initially a logical reference.



No database foreign key will be created until the ownership boundary is established.



---



# 15. Brand Reference



Column:



```text

brand_id varchar(26)

```



The Product Service may reference a Brand.



Similar to Category, Brand ownership is not assumed to belong to the Product Service.



Therefore:



```text

brand_id

```



is initially a logical reference.



No database foreign key will be created until the ownership boundary is established.



---



# 16. Product Status



Column:



```text

status varchar(20) NOT NULL

```



Initial values:



```text

DRAFT

ACTIVE

INACTIVE

ARCHIVED

```



Database constraint:



```sql

CHECK (

&#x20;   status IN (

&#x20;       'DRAFT',

&#x20;       'ACTIVE',

&#x20;       'INACTIVE',

&#x20;       'ARCHIVED'

&#x20;   )

)

```



The database guarantees that the stored status is valid.



Lifecycle transition rules remain application/domain responsibilities.



For example, the database does not enforce whether:



```text

DRAFT → ACTIVE

```



is valid.



That decision belongs to the domain model.



---



# 17. Timestamps



The Product table contains:



```text

created_at timestamptz NOT NULL

updated_at timestamptz NOT NULL

```



`TIMESTAMPTZ` is used so timestamps represent an absolute point in time.



Application-level timestamps should use UTC.



Example:



```text

2026-08-23T10:30:00Z

```



Business logic must not depend on the local timezone of the database server.



The exact timestamp-generation strategy will be finalized during implementation.



---



# 18. Product Table Example



Conceptual SQL:



```sql

CREATE TABLE products (

&#x20;   id VARCHAR(26) PRIMARY KEY,



&#x20;   sku VARCHAR(100) NOT NULL,

&#x20;   name VARCHAR(255) NOT NULL,

&#x20;   description TEXT,



&#x20;   price NUMERIC(19, 4) NOT NULL,

&#x20;   currency VARCHAR(3) NOT NULL,



&#x20;   category_id VARCHAR(26),

&#x20;   brand_id VARCHAR(26),



&#x20;   status VARCHAR(20) NOT NULL,



&#x20;   created_at TIMESTAMPTZ NOT NULL,

&#x20;   updated_at TIMESTAMPTZ NOT NULL,



&#x20;   CONSTRAINT uk_products_sku

&#x20;       UNIQUE (sku),



&#x20;   CONSTRAINT ck_products_price

&#x20;       CHECK (price >= 0),



&#x20;   CONSTRAINT ck_products_status

&#x20;       CHECK (

&#x20;           status IN (

&#x20;               'DRAFT',

&#x20;               'ACTIVE',

&#x20;               'INACTIVE',

&#x20;               'ARCHIVED'

&#x20;           )

&#x20;       )

);

```



This is the logical schema definition.



The actual database schema will be created through versioned migrations.



---



# 19. Index Strategy



Indexes are based on the API access patterns defined in `API_DESIGN.md`.



Initial indexes should be limited to known query patterns.



Required:



```text

PRIMARY KEY(id)

UNIQUE(sku)

```



Initial filtering indexes:



```text

status

category_id

brand_id

```



The initial implementation should not create indexes simply because a column exists.



Every additional index has:



* Storage cost

* Insert/update cost

* Maintenance cost



Additional indexes will be introduced based on actual query performance.



---



# 20. Initial Product Indexes



Conceptual SQL:



```sql

CREATE INDEX idx_products_status

&#x20;   ON products(status);



CREATE INDEX idx_products_category_id

&#x20;   ON products(category_id);



CREATE INDEX idx_products_brand_id

&#x20;   ON products(brand_id);

```



The unique SKU constraint automatically provides an index suitable for SKU lookup.



Indexes on `created_at` and `updated_at` are intentionally not mandatory in the first migration.



They may be added later if query plans and performance testing justify them.



---



# 21. Pagination and Indexing



The API supports page-based pagination:



```text

GET /api/v1/products?page=0&size=20

```



The initial implementation can use PostgreSQL pagination.



As data volume grows, large offset-based queries may become less efficient.



If scale requires it, cursor/keyset pagination can be introduced in a future API version or compatible extension.



This is intentionally deferred.



---



# 22. Search



The initial API supports simple product search:



```text

GET /api/v1/products?search=iphone

```



The first implementation may use PostgreSQL search capabilities.



A dedicated search engine such as Elasticsearch or OpenSearch will not be introduced initially.



A dedicated search engine will only be introduced if:



* Search requirements become significantly more complex

* PostgreSQL search performance becomes insufficient

* Relevance/ranking requirements justify it

* Product catalog scale requires it



---



# 23. Soft Delete



The Product table will not initially contain:



```text

deleted_at

```



Products are managed through lifecycle states:



```text

DRAFT

ACTIVE

INACTIVE

ARCHIVED

```



This keeps business lifecycle separate from technical deletion.



Physical deletion is not part of the initial Product API.



---



# 24. Database Constraints



Critical persistence invariants should be protected at the database level.



Initial constraints:



```text

PRIMARY KEY

UNIQUE SKU

PRICE >= 0

VALID STATUS

NOT NULL required fields

```



Application-level validation remains necessary.



Database constraints provide the final protection against invalid persistence.



---



# 25. Transaction Boundaries



Product changes must be transactional.



For a Product state change that produces an event:



```text

BEGIN

&#x20;  │

&#x20;  ├── Validate / modify Product

&#x20;  │

&#x20;  ├── Persist Product

&#x20;  │

&#x20;  └── Insert Outbox Event

&#x20;  │

COMMIT

```



If any step fails:



```text

ROLLBACK

```



Neither the Product change nor its corresponding Outbox event should be committed.



---



# 26. Transactional Outbox



The Product Service uses the transactional outbox pattern for reliable event publication.



Without an outbox, the following failure is possible:



```text

Product saved successfully

&#x20;       │

&#x20;       ▼

Application crashes

&#x20;       │

&#x20;       ▼

Kafka event never published

```



Instead:



```text

BEGIN TRANSACTION

&#x20;     │

&#x20;     ├── Save Product

&#x20;     │

&#x20;     └── Save Outbox Event

&#x20;     │

&#x20;    COMMIT

&#x20;     │

&#x20;     ▼

Outbox Relay

&#x20;     │

&#x20;     ▼

Kafka

```



This ensures the Product state change and pending event are committed atomically.



---



# 27. Outbox Events Table



Table:



```text

outbox_events

```



Logical structure:



| Column         | PostgreSQL Type | Nullable | Purpose               |

| -------------- | --------------- | -------: | --------------------- |

| id             | varchar(26)     |       No | Event identifier      |

| aggregate_type | varchar(100)    |       No | Aggregate type        |

| aggregate_id   | varchar(26)     |       No | Product identifier    |

| event_type     | varchar(150)    |       No | Event name            |

| payload        | jsonb           |       No | Serialized event      |

| created_at     | timestamptz     |       No | Event creation time   |

| published_at   | timestamptz     |      Yes | Publication timestamp |



---



# 28. Outbox Table Example



Conceptual SQL:



```sql

CREATE TABLE outbox_events (

&#x20;   id VARCHAR(26) PRIMARY KEY,



&#x20;   aggregate_type VARCHAR(100) NOT NULL,

&#x20;   aggregate_id VARCHAR(26) NOT NULL,



&#x20;   event_type VARCHAR(150) NOT NULL,



&#x20;   payload JSONB NOT NULL,



&#x20;   created_at TIMESTAMPTZ NOT NULL,

&#x20;   published_at TIMESTAMPTZ

);

```



The exact event payload and event contract will be defined as part of the event design.



---



# 29. Outbox Publisher Query



The Outbox Publisher needs to efficiently find unpublished events.



Conceptual query:



```sql

SELECT *

FROM outbox_events

WHERE published_at IS NULL

ORDER BY created_at;

```



A partial index should support this query:



```sql

CREATE INDEX idx_outbox_events_unpublished

&#x20;   ON outbox_events(created_at)

&#x20;   WHERE published_at IS NULL;

```



This index intentionally excludes already-published events.



---



# 30. Outbox Processing



The Outbox Publisher identifies unpublished events:



```text

published_at IS NULL

```



The publisher then:



1. Reads pending events

2. Publishes events to Kafka

3. Marks successfully published events

4. Continues processing remaining events



The exact concurrency, retry, locking, and batching strategy belongs in the Low-Level Design.



---



# 31. Outbox Delivery Semantics



The outbox provides reliable event publication from the database transaction.



It does not guarantee exactly-once delivery to Kafka.



The system should assume that an event may be delivered more than once.



Consumers must therefore be designed to tolerate duplicate events.



An event ID should be used to support consumer-side deduplication where necessary.



---



# 32. Outbox Retention



Published events should not necessarily remain forever.



A future retention policy may:



* Retain events for a defined period

* Archive events

* Delete old published records



The initial implementation may retain published records until an operational cleanup strategy is established.



No automatic deletion policy is required for the first version.



---



# 33. Database Migration Strategy



Database schema changes will be managed using versioned migrations.



The implementation will use one migration framework.



Candidates:



```text

Flyway

Liquibase

```



The final choice will be made when the Spring Boot project is initialized.



Production schemas must never depend on manually executed SQL scripts.



---



# 34. Migration Principles



Migrations must be:



* Versioned

* Reviewable

* Repeatable where appropriate

* Executed automatically during deployment

* Backward-compatible where practical



Conceptual example:



```text

V1__create_products.sql

V2__create_outbox_events.sql

V3__add_product_indexes.sql

```



Exact naming depends on the selected migration framework.



---



# 35. Development Database



Local development uses PostgreSQL through Docker Compose.



Existing infrastructure:



```text

infrastructure/

└── docker/

&#x20;   └── docker-compose.yml

```



The local database should be disposable.



Developers should be able to recreate the environment using Docker Compose and database migrations without manually creating schema objects.



---



# 36. Environment Configuration



Database configuration must be externalized.



Conceptual configuration:



```text

DB_HOST

DB_PORT

DB_NAME

DB_USERNAME

DB_PASSWORD

```



Secrets must not be committed to Git.



Local development values may be provided through:



```text

environment variables

.env

Docker secrets

```



depending on the environment.



---



# 37. Connection Pooling



The Product Service will use a database connection pool.



The initial Spring Boot implementation will use the framework's standard connection-pooling mechanism.



Initial pool sizing will not be hard-coded in this architecture document.



Pool values will be established using:



* Expected concurrency

* Database capacity

* Performance testing

* Deployment topology



---



# 38. PostgreSQL and Kafka Transactions



Kafka publication is not treated as part of the PostgreSQL transaction.



The Product Service uses:



```text

PostgreSQL Transaction

&#x20;       │

&#x20;       ├── Product change

&#x20;       └── Outbox event

&#x20;               │

&#x20;             COMMIT

&#x20;               │

&#x20;               ▼

&#x20;       Outbox Publisher

&#x20;               │

&#x20;               ▼

&#x20;             Kafka

```



This avoids requiring a distributed transaction across PostgreSQL and Kafka.



---



# 39. Referential Integrity



The Product Service owns Product data.



References such as:



```text

category_id

brand_id

```



will not initially use database foreign keys if the referenced entities are owned by other services.



The Product Service may validate external references through application-level interactions or other mechanisms when required.



This preserves service ownership boundaries.



---



# 40. Data Ownership



The Product Service owns:



```text

Product

Product lifecycle

Product SKU

Product pricing information

Product descriptive information

```



It does not own:



```text

Inventory

Orders

Payments

Customers

```



Those capabilities belong to other services.



---



# 41. Auditability



The initial Product table provides:



```text

created_at

updated_at

```



A complete historical audit trail is not required in the initial implementation.



A dedicated audit mechanism may be introduced later if compliance or business requirements require:



* Who changed a Product

* What changed

* When it changed

* Previous values



---



# 42. Data Consistency



The system uses different consistency models for different concerns.



Product state:



```text

Strong consistency

```



within PostgreSQL transactions.



Event propagation:



```text

Eventually consistent

```



through the transactional outbox and Kafka.



Example:



```text

Product updated

&#x20;     │

&#x20;     ▼

PostgreSQL committed

&#x20;     │

&#x20;     ▼

Outbox event available

&#x20;     │

&#x20;     ▼

Kafka publication

&#x20;     │

&#x20;     ▼

Other services receive event

```



This distinction is intentional.



---



# 43. Database Performance Principles



Initial performance priorities:



1. Correctness

2. Appropriate indexes

3. Efficient queries

4. Connection pool management

5. Transaction boundaries

6. Monitoring

7. Performance testing



Premature optimization should be avoided.



Indexes should be justified by known query patterns or measured performance requirements.



---



# 44. Database Security



The Product Service database must:



* Use authenticated database connections

* Use least-privilege database users

* Avoid exposing PostgreSQL directly to the public internet

* Use encrypted connections in production where required

* Store credentials outside source control

* Restrict administrative database access



Local development may use simpler Docker-based configuration.



---



# 45. Backup and Recovery



Production PostgreSQL must eventually have:



* Automated backups

* Point-in-time recovery where appropriate

* Backup retention policy

* Restore testing

* Disaster recovery procedures



These operational concerns are outside the initial local development setup.



---



# 46. Database Monitoring



Production monitoring should eventually track:



```text

Connection pool usage

Query latency

Slow queries

Transaction duration

Database CPU

Database memory

Disk usage

Replication status

Lock contention

Deadlocks

Outbox backlog

```



The exact monitoring stack will be defined later.



---



# 47. Initial Database Structure



The initial Product Service database is intentionally small:



```text

PostgreSQL

│

└── public

&#x20;   │

&#x20;   ├── products

&#x20;   │

&#x20;   └── outbox_events

```



This is sufficient for the first implementation.



Additional tables will only be introduced when required by the domain.



---



# 48. Example Product Record



Conceptual database record:



```text

id:

01JABC123XYZ



sku:

IPHONE-15-128-BLK



name:

iPhone 15



description:

Apple iPhone 15 with 128GB storage



price:

799.9900



currency:

USD



category_id:

01JCATEGORY001



brand_id:

01JBRAND001



status:

ACTIVE



created_at:

2026-08-23T10:30:00Z



updated_at:

2026-08-23T10:30:00Z

```



---



# 49. API-to-Database Mapping



The API Product representation maps conceptually to:



```text

API Field       → Database Column



id              → id

sku             → sku

name            → name

description     → description

price           → price

currency        → currency

categoryId      → category_id

brandId         → brand_id

status          → status

createdAt       → created_at

updatedAt       → updated_at

```



The API DTO and persistence entity do not need to be the same Java object.



---



# 50. Database Design Non-Goals



The initial database design does not include:



* Inventory tables

* Order tables

* Payment tables

* Customer tables

* Product image storage

* Product review tables

* Recommendation data

* Search engine indexes

* Analytics warehouse tables

* Distributed database architecture

* Database sharding



These concerns will be addressed only when their corresponding capabilities are introduced.



---



# 51. Database Design Status



**Status:** Draft



This document defines the initial PostgreSQL persistence design for the Product Service.



It has been aligned with:



* BRD

* FRD

* Domain Model

* HLD

* API Design



The next step is to turn this logical design into executable database migrations.



---



# 52. Next Steps



After database design approval:



1. Select Flyway or Liquibase

2. Create the initial Product Service project

3. Create database migrations

4. Configure PostgreSQL connectivity

5. Implement the domain model

6. Implement persistence entities

7. Implement repositories

8. Implement application services

9. Implement REST controllers

10. Implement automated tests

11. Implement transactional outbox

12. Add OpenAPI documentation

13. Run the Product Service locally

14. Validate the complete API against PostgreSQL



````



### Final verdict



**`DATABASE_DESIGN.md` is now approved for implementation. ✅**



The two changes from the previous review are incorporated:



- `currency` → `VARCHAR(3)`

- Initial indexes kept deliberately minimal

- Partial index added for unpublished outbox events

- Outbox duplicate-delivery semantics explicitly documented

- ULID generation responsibility explicitly assigned to the application



You can now commit it:



```powershell

git add docs/architecture/DATABASE_DESIGN.md

git commit -m "docs: define product service database design"

git push origin main

````



After that, we're done with the **logical architecture/design documents**.



**Next: we stop writing documents and start creating the actual Spring Boot project.**



