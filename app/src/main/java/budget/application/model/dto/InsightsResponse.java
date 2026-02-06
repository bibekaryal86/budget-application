package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.util.List;

public record InsightsResponse() {
  public record CashFlowSummaries(List<CashFlowSummary> data, ResponseMetadata metadata) {}

  public record CashFlowSummary(String yearMonth, CashFlowAmounts cashFlowAmounts) {}

  public record CashFlowAmounts(
      BigDecimal incomes, BigDecimal expenses, BigDecimal savings, BigDecimal balance) {}

  public record CategorySummaries(List<CategorySummary> data, ResponseMetadata metadata) {}

  public record CategorySummary(String yearMonth, List<CategoryAmount> categoryAmounts) {}

  public record CategoryAmount(CategoryResponse.Category category, BigDecimal amount) {}
}
