package budget.application.model.dto;

import java.util.UUID;

public record TransactionItemRequest(
    UUID transactionId, UUID categoryId, String label, double amount, String expType) {}
