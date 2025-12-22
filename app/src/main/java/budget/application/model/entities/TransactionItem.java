package budget.application.model.entities;

import java.util.UUID;
import lombok.Builder;

@Builder
public record TransactionItem(
    UUID id, UUID transactionId, UUID categoryId, String label, double amount) {}
