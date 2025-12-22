package budget.application.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Transaction(
    UUID id,
    LocalDateTime txnDate,
    String description,
    double totalAmount,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
