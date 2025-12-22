package budget.application.model.dto.request;

import java.util.UUID;

public record CategoryRequest(UUID categoryTypeId, String name) {}
