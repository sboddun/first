# Apache Camel REST API with Spring Boot - AI Coding Guide

## Architecture Overview

This is a **Spring Boot 3.5.10 + Apache Camel 4.14.4** REST API project demonstrating REST endpoints with SQL integration and comprehensive validation. The application follows a **Camel route-based architecture** where:

- **REST endpoints** are defined using Camel REST DSL in `RouteBuilder` classes
- **Request processing** flows through Camel routes (`direct:` endpoints)
- **Validation** happens via Bean Validation (`bean-validator:` component) with custom validators
- **Exception handling** is centralized using Camel's `onException()` DSL
- **Database operations** use Camel's SQL component with PostgreSQL (H2 in tests)

## Key Components and Patterns

### Route Structure Pattern
All routes extend `RouteBuilder` and are Spring `@Component` beans:
```java
@Component
public class CustomerRestService extends RouteBuilder {
  // REST DSL defines endpoints -> routes to direct: endpoints -> processing pipeline
  restConfiguration().component("platform-http").bindingMode(RestBindingMode.json);
  rest("/customers").get("/{customerId}").to("direct:getCustomerById");
  from("direct:getCustomerById").process(...).to("bean-validator:...").to("sql:...");
}
```

### Validation Architecture
**Two-tier validation system:**
1. **Field-level**: Jakarta Bean Validation annotations on model classes (`@NotEmpty`, `@Pattern`, `@Min`, `@Max`)
2. **Class-level**: Custom validators implementing `ConstraintValidator` (see `DateRangeValidator`)
   - Applied via custom annotation (`@ValidDateRange` on `CustomerRequest`)
   - Validates cross-field logic (e.g., startDate < endDate)

**Critical**: The `bean-validator:validateCustomerRequest` component integrates Bean Validation into Camel routes.

### Exception Handling Pattern
Use Camel's `onException()` DSL in routes (NOT Spring's `@ControllerAdvice`):
```java
private void configureExceptionHandlers() {
  onException(org.apache.camel.ValidationException.class)
    .handled(true)
    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
    .process(validationExceptionProcessor);  // Transforms to ErrorResponse JSON
}
```

**Processors** (`ValidationExceptionProcessor`, `ServerExceptionProcessor`) extract constraint violations and format error responses.

### Model Objects
- **Immutable request models**: Final fields, all-args constructor (see `CustomerRequest`)
- **Factory method pattern**: `CustomerRequest.fromExchange(Exchange)` extracts path/query params
- **Response models**: `ErrorResponse` (validation/server errors), `SuccessResponse` (business data)

### SQL Integration
- Use Camel SQL component with named parameters: `sql:INSERT ... :#${body.fieldName}`
- DataSource injected as Spring bean (`#dataSource` reference in SQL URI)
- Route body contains the model object; Camel extracts fields automatically

## Project Structure

- **routes/**: RouteBuilder classes defining REST endpoints and processing logic
- **processors/**: Camel Processor beans for custom exchange manipulation
- **model/**: Request/response DTOs with validation annotations
- **utils/**: Custom validators (e.g., `DateRangeValidator`, `ValidDateRange`)
- **exceptions/**: Custom exception classes (currently unused, using Camel exceptions)

## Build & Test

**Build**: `./gradlew clean build`
**Test**: `./gradlew test`
**Run**: `./gradlew bootRun` (requires PostgreSQL on localhost:5432 with database `mydb`, user `admin`, password `admin`)

**Test Strategy**:
- Use `@CamelSpringBootTest` + `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- `TestConfig` provides H2 embedded DB for tests (see [TestConfig.java](src/test/java/com/insulet/practice/first/routes/TestConfig.java))
- Test both success (200) and validation error (400) scenarios
- Use `TestRestTemplate` for HTTP calls, `UriComponentsBuilder` for query params
- Database schema auto-created from [schema.sql](src/test/resources/schema.sql)

## Critical Conventions

1. **REST paths** use `/data-share/v1` context path (configured in `restConfiguration()`)
2. **Date format**: ISO 8601 with milliseconds (`2024-01-01T00:00:00.000Z`)
   - Validated via `@Pattern` regex on model fields
3. **PostgreSQL column naming**: Snake_case (`customer_id`, `start_date`, `rec_limit`)
4. **Pagination**: `page` (0-based) and `limit` (max 500) query parameters
5. **HTTP status codes**: 200 (success), 400 (validation), 500 (server error)

## Dependencies

- **Camel BOM** (`camel-spring-boot-bom:4.14.4`) manages all Camel versions
- Key starters: `camel-rest-starter`, `camel-platform-http-starter`, `camel-sql-starter`, `camel-bean-validator-starter`
- **Monitoring**: Micrometer + Datadog StatsD (metrics exported to DD agent sidecar)
- **DB**: PostgreSQL (runtime), H2 (tests)

## Monitoring & Observability

**Local Development**:
- **Actuator endpoints**: `/actuator/health`, `/actuator/metrics`
- **Camel metrics**: Enabled via `management.metrics.enable.camel=true`
- **SQL logging**: Set to DEBUG for query/parameter visibility in console
- **Datadog StatsD**: Configured but optional locally (defaults to `localhost:8125`)
  - Set `management.metrics.export.statsd.enabled=false` in local profile to disable

**Production (ECS)**:
- **Datadog integration**: StatsD exporter sends metrics to sidecar agent
- **ECS metadata tags**: Injected via environment variables (`DD_SERVICE`, `DD_ENV`, `DD_VERSION`, `ECS_TASK_FAMILY`, `ECS_CLUSTER`)
- **Agent host**: Resolved via `DD_AGENT_HOST` env var (points to sidecar container)
- Metrics include Camel route stats, JVM metrics, and custom application metrics

## When Adding New Endpoints

1. Create new `RouteBuilder` class in `routes/`
2. Define REST endpoint using `rest()` DSL
3. Create `direct:` route for processing logic
4. Add validation annotations to request model
5. Configure exception handlers with `onException()`
6. Add processor beans if custom error formatting needed
7. Write tests covering success + all validation failure cases

## Common Gotchas

- **Camel vs Spring REST**: This project uses Camel REST DSL, not Spring MVC controllers
- **Validation component**: Must use `bean-validator:` in route, not Spring's `@Valid`
- **Exception handling**: Use Camel's `onException()`, not `@ExceptionHandler`
- **SQL component**: Named parameters use `:#${...}` syntax, not `?` or standard `:name`
- **Platform HTTP**: Uses `platform-http` component (embedded in Spring Boot), not standalone Jetty

## OpenAPI Spec

See [first-api-spec.yaml](src/main/resources/first-api-spec.yaml) for complete API documentation with request/response schemas and validation rules.
