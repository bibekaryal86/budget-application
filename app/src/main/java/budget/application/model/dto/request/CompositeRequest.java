package budget.application.model.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record CompositeRequest(
        TransactionComposite transactionComposite, CategoryComposite categoryComposite) {

  public record TransactionComposite(
      LocalDate beginDate,
      LocalDate endDate,
      String merchant,
      UUID categoryId,
      UUID categoryTypeId) {}

  public record CategoryComposite(UUID categoryTypeId) {}
}
