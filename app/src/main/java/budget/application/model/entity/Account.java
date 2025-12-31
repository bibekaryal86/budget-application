package budget.application.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record Account(
    UUID id,
    String name,
    String accountType,
    String bankName,
    double openingBalance,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
