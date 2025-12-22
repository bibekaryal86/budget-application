package budget.service.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Category(
    UUID id, UUID categoryTypeId, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
