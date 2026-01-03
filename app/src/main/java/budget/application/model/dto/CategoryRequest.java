package budget.application.model.dto;

import java.util.UUID;

public record CategoryRequest(UUID categoryTypeId, String name) {}
