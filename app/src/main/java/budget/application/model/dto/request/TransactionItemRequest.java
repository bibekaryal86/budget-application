package budget.application.model.dto.request;

import java.util.UUID;

public record TransactionItemRequest(UUID categoryId, String label, double amount) {}
