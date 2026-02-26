package budget.application.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionRequest(
    LocalDateTime txnDate,
    String merchant,
    BigDecimal totalAmount,
    String notes,
    List<TransactionItemRequest> items) {}
