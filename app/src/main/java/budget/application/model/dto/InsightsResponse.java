package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record InsightsResponse() {
  public record CashFlowSummaries(List<CashFlowSummary> data, ResponseMetadata metadata) {}

  public record CashFlowSummary(String yearMonth, CashFlowAmounts cashFlowAmounts) {}

  public record CashFlowAmounts(
      BigDecimal incomes, BigDecimal expenses, BigDecimal savings, BigDecimal balance) {}

  public record CategorySummaries(List<CategorySummary> data, ResponseMetadata metadata) {}

  public record CategorySummary(String yearMonth, List<CategoryAmount> categoryAmounts) {}

  public record CategoryAmount(CategoryResponse.Category category, BigDecimal amount) {}

  public record AccountSummaries(List<AccountSummary> data, ResponseMetadata metadata) {}

  public record AccountSummary(
      String yearMonth, Map<String, BigDecimal> netWorth, List<AccountResponse.Account> accounts) {}
}
