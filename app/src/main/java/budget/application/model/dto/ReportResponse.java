package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(List<TransactionSummary> data, ResponseMetadata metadata) {
  public record TransactionSummary(
      LocalDateTime beginDate,
      LocalDateTime endDate,
      BigDecimal incomes,
      BigDecimal expenses,
      BigDecimal savings) {}
}
