package budget.application.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionItemRequest(
    UUID transactionId, UUID categoryId, String label, BigDecimal amount, String expType) {}
