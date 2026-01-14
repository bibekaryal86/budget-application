# budget-service

A streamlined, lightweight personal finance tracker built with a focus on high performance and zero framework overhead.
This application provides a robust backend for managing expenses and income through a structured category system.

## Technical Stack
This project is built from the ground up without heavy frameworks like Spring or Micronaut to ensure a small footprint
and deep understanding of the underlying protocols.

* Language: Java
* Networking: Netty (Async event-driven network application framework)
* Database: PostgreSQL
* Migration Tool: Flyway (Version control for database)
* Data Access: JDBC (Raw SQL execution for maximum transparency)

## Data Model
The application uses a hierarchical structure to organize financial data, allowing for granular tracking of spending habits.

### Core Components
* Category Types: Top-level classifications (e.g., "Income," "Travel & Vacation," "Shopping").
* Categories: Specific buckets for spending (e.g., "Groceries," "Rent," "Streaming Services").
* Transactions: The record of a financial event at a specific date and time.
* Transaction Items: Line items within a single transaction, allowing you to split a single receipt (e.g., a Target run) into multiple categories.

## Roadmap & Future Enhancements
* OpenAPI/Swagger: Provide a simple REST API specification for external integrations.
* Banks & Accounts: Integration to track multiple physical bank accounts and credit cards.
* Summary Engine: Automated monthly and yearly reports to visualize spending trends.
* Budgeting Goals: Set limits on specific categories and track progress.
* Audit Trail: Track changes to data over time.

## Flyway
* Run flyway command as `./gradlew flywayMigrate`
    * For first run, append `-Dflyway.baselineOnMigrate=true` to set baseline migration
      * `./gradlew -Dorg.gradle.configuration-cache=false flywayMigrate -Dflyway.baselineOnMigrate=true`
    * PRE-REQUISITES for flyway
        * Build needs to be run first
            * `./gradlew clean build -x test`
        * Flyway plugin does not work with gradle's configuration cache
            * `./gradlew -Dorg.gradle.configuration-cache=false flywayMigrate`
* Clear database (DELETES EVERYTHING)
    * `./gradlew -Dorg.gradle.configuration-cache=false flywayClean -Dflyway.cleanDisabled=false`
* Flyway migration is configured to not trigger automatically, it only validates
    * This means that migration command needs to be given manually
* Flyway migration is controlled via github actions to main DB branch
* There are 2 database instances created to support local development and production data
    * `pets-service`
        * This instance is used for production instance
        * When a pull request is merged to main branch, flyway migration is run in this branch
    * `pets-service-sandbox`
        * This branch is used for local/development instances
        * When a pull request is created, flyway migration is run in this branch to validate schema changes

