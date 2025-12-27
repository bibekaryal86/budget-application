package budget.application.model.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record CompositeRequest(
    TransactionRequest transactionRequest, CategoryRequest categoryRequest) {

  public record TransactionRequest(
      LocalDate beginDate,
      LocalDate endDate,
      String merchant,
      UUID categoryId,
      UUID categoryTypeId) {}

  public record CategoryRequest(UUID categoryTypeId) {}
}
