package budget.application.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record TransactionRequest(
    LocalDateTime txnDate,
    String merchant,
    double totalAmount,
    String notes,
    List<TransactionItemRequest> items) {}
