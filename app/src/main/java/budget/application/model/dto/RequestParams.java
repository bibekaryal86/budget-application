package budget.application.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequestParams(TransactionParams transactionParams, CategoryParams categoryParams) {

  public record TransactionParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<String> merchants,
      List<UUID> categoryIds,
      List<UUID> categoryTypeIds,
      List<UUID> accountIds,
      List<String> tags) {}

  public record CategoryParams(List<UUID> categoryTypeIds) {}

  public record BudgetParams(int budgetMonth, int budgetYear, List<UUID> categoryIds) {}

  public record CashFlowSummaryParams(LocalDate beginDate, LocalDate endDate) {}

  public record CategorySummaryParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<UUID> categoryIds,
      List<UUID> categoryTypeIds,
      boolean topExpenses) {}
}
