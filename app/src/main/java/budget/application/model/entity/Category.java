package budget.application.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record Category(
    UUID id, UUID categoryTypeId, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
