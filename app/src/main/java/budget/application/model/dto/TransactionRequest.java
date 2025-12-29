package budget.application.model.dto;

import java.time.LocalDate;
import java.util.List;

public record TransactionRequest(
    LocalDate txnDate,
    String merchant,
    double totalAmount,
    String notes,
    List<TransactionItemRequest> items) {}
