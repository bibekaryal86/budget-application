package budget.application.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Account(
    UUID id,
    String name,
    String accountType,
    String bankName,
    BigDecimal accountBalance,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
