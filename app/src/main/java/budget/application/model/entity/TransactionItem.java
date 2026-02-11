package budget.application.model.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionItem(
    UUID id,
    UUID transactionId,
    UUID categoryId,
    UUID accountId,
    BigDecimal amount,
    List<String> tags,
    String notes) {}
