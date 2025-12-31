package budget.application.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionRequest(
    LocalDateTime txnDate,
    String merchant,
    UUID accountId,
    double totalAmount,
    String notes,
    List<TransactionItemRequest> items) {}
