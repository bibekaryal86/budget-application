package budget.service.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CategoryType(
    UUID id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
