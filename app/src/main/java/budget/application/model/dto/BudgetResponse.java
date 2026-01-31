package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(List<Budget> data, ResponseMetadata metadata) {
  public record Budget(
      UUID id,
      CategoryResponse.Category category,
      int budgetMonth,
      int budgetYear,
      BigDecimal amount,
      String notes) {}
}
