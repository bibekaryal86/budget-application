package budget.application.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionItemRequest(
    UUID transactionId,
    UUID categoryId,
    String label,
    BigDecimal amount,
    String expType,
    List<String> tags) {}
