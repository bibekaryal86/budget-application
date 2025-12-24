package budget.application.model.dto.request;

import java.time.LocalDate;

public record CompositeRequest(TransactionRequest transactionRequest) {

  public record TransactionRequest(
      LocalDate beginDate,
      LocalDate endDate,
      String merchant,
      String categoryId,
      String categoryTypeId) {}
}
