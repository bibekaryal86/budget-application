package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(TransactionInsightsResponse data, ResponseMetadata metadata) {
  public record TransactionInsightsResponse(
      List<Transaction> transactions,
      InsightsResponse.CashFlowAmounts cashFlowAmounts,
      List<InsightsResponse.CategoryAmount> categoryAmounts,
      List<InsightsResponse.AccountAmount> accountAmounts) {}

  public record Transaction(
      UUID id,
      LocalDateTime txnDate,
      String merchant,
      BigDecimal totalAmount,
      List<TransactionItemResponse.TransactionItem> items) {}

  public record TransactionMerchants(List<String> data, ResponseMetadata metadata) {}
}
