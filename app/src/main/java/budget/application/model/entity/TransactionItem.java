package budget.application.model.entity;

import java.util.UUID;

public record TransactionItem(
    UUID id, UUID transactionId, UUID categoryId, String label, double amount, String expType) {}
