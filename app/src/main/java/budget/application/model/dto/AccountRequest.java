package budget.application.model.dto;

public record AccountRequest(
    String name, String accountType, String bankName, double openingBalance, String status) {}
