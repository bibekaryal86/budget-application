package budget.application.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequestParams(
    TransactionParams transactionParams,
    CategoryParams categoryParams,
    TransactionItemParams transactionItemParams) {

  public record TransactionParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<String> merchants,
      List<UUID> catIds,
      List<UUID> catTypeIds,
      List<String> txnTypes) {}

  public record CategoryParams(List<UUID> catTypesId) {}

  public record TransactionItemParams(
      List<UUID> txnIds, List<UUID> catIds, List<String> txnTypes) {}
}
