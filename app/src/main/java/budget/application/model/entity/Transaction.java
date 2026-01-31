package budget.application.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
    UUID id,
    LocalDateTime txnDate,
    String merchant,
    UUID accountId,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
