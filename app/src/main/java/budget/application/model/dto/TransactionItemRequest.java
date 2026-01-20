package budget.application.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionItemRequest(
    UUID transactionId, UUID categoryId, BigDecimal amount, List<String> tags, String notes) {}
