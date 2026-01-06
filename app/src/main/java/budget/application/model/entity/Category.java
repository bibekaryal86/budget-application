package budget.application.model.entity;

import java.util.UUID;

public record Category(UUID id, UUID categoryTypeId, String name) {}
