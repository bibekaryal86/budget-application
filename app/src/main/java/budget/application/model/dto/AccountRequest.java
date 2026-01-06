package budget.application.model.dto;

import java.math.BigDecimal;

public record AccountRequest(
    String name, String accountType, String bankName, BigDecimal openingBalance, String status) {}
