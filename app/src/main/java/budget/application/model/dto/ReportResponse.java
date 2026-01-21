package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportResponse(TransactionSummaries txnSummaries, ResponseMetadata metadata) {
  public record TransactionSummaries(
      TransactionSummary currentMonth, TransactionSummary previousMonth) {}

  public record TransactionSummary(
      LocalDateTime beginDate,
      LocalDateTime endDate,
      BigDecimal incomes,
      BigDecimal expenses,
      BigDecimal savings) {}
}
