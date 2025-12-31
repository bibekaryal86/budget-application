package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;
import java.util.UUID;

public record TransactionItemResponse(List<TransactionItem> data, ResponseMetadata metadata) {
  public record TransactionItem(
      UUID id,
      TransactionResponse.Transaction transaction,
      CategoryResponse.Category category,
      String label,
      double amount,
      String expType) {}
}
