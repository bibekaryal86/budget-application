package budget.application.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
    UUID id,
    LocalDate txnDate,
    String merchant,
    double totalAmount,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
