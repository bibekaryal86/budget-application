package budget.application.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryType(
    UUID id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
