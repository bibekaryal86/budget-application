package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InsightsResponse() {
  public record CashFlowSummaries(
      CashFlowSummary currentMonth, CashFlowSummary previousMonth, ResponseMetadata metadata) {}

  public record CashFlowSummary(
      LocalDate beginDate,
      LocalDate endDate,
      BigDecimal incomes,
      BigDecimal expenses,
      BigDecimal savings) {}

  public record CategorySummaries(
      List<CategorySummary> currentMonth,
      List<CategorySummary> previousMonth,
      ResponseMetadata metadata) {}

  public record CategorySummary(
      LocalDate beginDate,
      LocalDate endDate,
      CategoryResponse.Category category,
      BigDecimal amount) {}
}
