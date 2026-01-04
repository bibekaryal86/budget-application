package budget.application.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequest(
    UUID categoryId, int budgetMonth, int budgetYear, BigDecimal amount, String notes) {}
