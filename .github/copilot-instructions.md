# Cloud Agent Instructions for Order Service
## Technology Stack
- Java 17
- Spring Boot 3.2.x
- Apache Maven

## Architectural Rules
- All DTOs must be implemented as immutable Java 17 records in `com.example.orderservice.dto`.
- Business rules, volume discount logic, and SKU validation belong strictly in `OrderServiceImpl`.
- REST endpoints must adhere to HTTP conventions (POST returns 201 Created with Location header or body).

## Automated Validation Requirements
- Before opening a Pull Request, the cloud agent must execute `mvn clean test`.
- All JUnit 5 test cases must pass with zero failures.
- If a build fails, inspect `target/surefire-reports`, adjust code, and verify before PR submission.
