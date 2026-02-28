package budget.application.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequestParams() {

  public record TransactionParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<String> merchants,
      List<UUID> categoryIds,
      List<UUID> categoryTypeIds,
      List<UUID> accountIds,
      List<String> tags) {}

  public record BudgetParams(int budgetMonth, int budgetYear, List<UUID> categoryIds) {}

  public record CashFlowSummaryParams(LocalDate beginDate, LocalDate endDate, int totalMonths) {}

  public record CategorySummaryParams(
      LocalDate beginDate,
      LocalDate endDate,
      List<UUID> categoryIds,
      List<UUID> categoryTypeIds,
      int topExpenses,
      int totalMonths) {}

  public record AccountSummaryParams(
      LocalDate beginDate, LocalDate endDate, List<UUID> accountIds) {}
}
