package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(List<Transaction> data, ResponseMetadata metadata) {
  public record Transaction(
      UUID id,
      LocalDateTime txnDate,
      String merchant,
      BigDecimal totalAmount,
      String notes,
      AccountResponse.Account account,
      List<TransactionItemResponse.TransactionItem> items) {}

  public record TransactionMerchants(List<String> data, ResponseMetadata metadata) {}
}
