package budget.application.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequestParams(TransactionParams transactionParams, CategoryParams categoryParams) {

  public record TransactionParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<String> merchants,
      List<UUID> catIds,
      List<UUID> catTypeIds,
      List<UUID> accIds) {}

  public record CategoryParams(List<UUID> catTypesId) {}

  public record BudgetParams(int budgetMonth, int budgetYear, List<UUID> catIds) {}
}
