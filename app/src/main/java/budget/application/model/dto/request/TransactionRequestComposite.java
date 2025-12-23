package budget.application.model.dto.request;

import java.time.LocalDate;

public record TransactionRequestComposite(
    LocalDate beginDate,
    LocalDate endDate,
    String merchant,
    String categoryId,
    String categoryTypeId) {}
