package budget.application.model.dto;

import budget.application.common.Constants;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record InsightsResponse() {
  public record CashFlowSummaries(List<CashFlowSummary> data, ResponseMetadata metadata) {}

  public record CashFlowSummary(String yearMonth, CashFlowAmounts cashFlowAmounts) {}

  public record CashFlowAmounts(
      BigDecimal incomes, BigDecimal expenses, BigDecimal savings, BigDecimal balance) {}

  public record CategorySummaries(List<CategorySummary> data, ResponseMetadata metadata) {}

  public record CategorySummary(String yearMonth, List<CategoryAmount> categoryAmounts) {}

  public record CategoryAmount(CategoryResponse.Category category, BigDecimal amount) {}

  public record AccountSummaries(List<AccountSummary> data, ResponseMetadata metadata) {}

  public record AccountSummary(
      String yearMonth, Map<String, BigDecimal> netWorth, List<AccountResponse.Account> accounts) {

    public static AccountSummary withNetWorth(
        String yearMonth, List<AccountResponse.Account> accounts) {
      BigDecimal assets = BigDecimal.ZERO;
      BigDecimal debts = BigDecimal.ZERO;

      for (AccountResponse.Account account : accounts) {
        BigDecimal balance = account.accountBalance();

        if (Constants.ASSET_ACCOUNT_TYPES.contains(account.accountType())
            || Constants.INVEST_ACCOUNT_TYPES.contains(account.accountType())) {
          assets = assets.add(balance);
        } else if (Constants.DEBT_ACCOUNT_TYPES.contains(account.accountType())) {
          debts = debts.add(balance);
        }
      }

      Map<String, BigDecimal> calculatedNetWorth =
          Map.of(
              "ASSETS", assets,
              "DEBTS", debts,
              "WORTH", assets.subtract(debts));

      return new AccountSummary(yearMonth, calculatedNetWorth, accounts);
    }
  }
}
