package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountResponse(List<Account> data, ResponseMetadata metadata) {
  public record Account(
      UUID id,
      String name,
      String accountType,
      String bankName,
      BigDecimal accountBalance,
      String status) {}

  // TODO remove this
  public record AccountCurrentBalanceCalc(
      UUID id, BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal totalTransfers) {}

  public record AccountRefLists(List<String> data, ResponseMetadata metadata) {}
}
