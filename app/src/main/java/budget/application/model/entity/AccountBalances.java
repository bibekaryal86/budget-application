package budget.application.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountBalances(
    UUID id,
    UUID accountId,
    String yearMonth,
    BigDecimal accountBalance,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
