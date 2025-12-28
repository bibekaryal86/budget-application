package budget.application.model.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompositeRequest(
    TransactionComposite transactionComposite, CategoryComposite categoryComposite) {

  public record TransactionComposite(
      LocalDate beginDate,
      LocalDate endDate,
      List<String> merchants,
      List<UUID> catIds,
      List<UUID> catTypeIds,
      List<String> txnTypes) {}

  public record CategoryComposite(List<UUID> catTypesId) {}
}
