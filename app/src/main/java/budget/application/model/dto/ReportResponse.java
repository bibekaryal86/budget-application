package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportResponse() {
  public record TransactionSummaries(
      TransactionSummary currentMonth,
      TransactionSummary previousMonth,
      ResponseMetadata metadata) {}

  public record TransactionSummary(
      LocalDate beginDate,
      LocalDate endDate,
      BigDecimal incomes,
      BigDecimal expenses,
      BigDecimal savings) {}

  public record CategorySummaries(
      LocalDate beginDate,
      LocalDate endDate,
      List<CategorySummary> cData,
      List<CategoryTypeSummary> ctData,
      ResponseMetadata metadata) {}

  public record CategorySummary(CategoryResponse.Category category, BigDecimal amount) {}

  public record CategoryTypeSummary(
      CategoryTypeResponse.CategoryType categoryType, BigDecimal amount) {}
}
