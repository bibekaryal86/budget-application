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
