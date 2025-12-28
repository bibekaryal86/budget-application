package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(List<Transaction> data, ResponseMetadata metadata) {
  public record Transaction(
      UUID id,
      LocalDateTime txnDate,
      String merchant,
      double totalAmount,
      String notes,
      List<TransactionItemResponse.TransactionItem> items) {}
}
