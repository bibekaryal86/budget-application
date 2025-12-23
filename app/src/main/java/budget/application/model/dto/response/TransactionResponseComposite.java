package budget.application.model.dto.response;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TransactionResponseComposite(
    List<TransactionComposite> data, ResponseMetadata metadata) {

  public record TransactionComposite(
      UUID id,
      LocalDate txnDate,
      String merchant,
      double totalAmount,
      String notes,
      List<TransactionItemComposite> items) {}

  public record TransactionItemComposite(UUID id, double amount, CategoryComposite category) {}

  public record CategoryComposite(UUID id, String name, CategoryTypeComposite categoryType) {}

  public record CategoryTypeComposite(UUID id, String name) {}
}
