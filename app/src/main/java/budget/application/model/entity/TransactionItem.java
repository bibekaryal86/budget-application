package budget.application.model.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionItem(
    UUID id,
    UUID transactionId,
    UUID categoryId,
    String label,
    BigDecimal amount,
    String expType) {}
