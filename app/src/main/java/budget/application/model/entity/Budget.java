package budget.application.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Budget(
    UUID id,
    UUID categoryId,
    int budgetMonth,
    int budgetYear,
    BigDecimal amount,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
