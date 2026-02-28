package budget.application.db.dao;

import budget.application.db.mapper.AccountBalancesRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.entity.AccountBalances;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AccountBalancesDao extends BaseDao<AccountBalances> {

  public AccountBalancesDao(Connection connection) {
    super(connection, new AccountBalancesRowMappers.AccountBalancesRowMapper(), null);
  }

  @Override
  protected String tableName() {
    return "account_balances";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("account_id", "year_month", "account_balance");
  }

  @Override
  protected List<Object> insertValues(AccountBalances accountBalances) {
    return List.of(
        accountBalances.accountId(), accountBalances.yearMonth(), accountBalances.accountBalance());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("account_id", "year_month", "account_balance", "notes");
  }

  @Override
  protected List<Object> updateValues(AccountBalances accountBalances) {
    return List.of(
        accountBalances.accountId(),
        accountBalances.yearMonth(),
        accountBalances.accountBalance(),
        accountBalances.notes());
  }

  @Override
  protected UUID getId(AccountBalances accountBalances) {
    return accountBalances.id();
  }

  @Override
  protected String orderByClause() {
    return "year_month DESC";
  }

  public int updateAccountBalances(
      UUID accountId, String yearMonth, BigDecimal accountBalance, String notes)
      throws SQLException {
    String sql =
        "UPDATE account_balances "
            + "SET account_balance = ?, "
            + "notes = COALESCE(notes, '') || ? "
            + "WHERE account_id = ? AND year_month = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(
          preparedStatement, List.of(accountBalance, notes, accountId, yearMonth), Boolean.FALSE);
      return preparedStatement.executeUpdate();
    }
  }

  public int deleteAccountBalances(List<UUID> accountIds) throws SQLException {
    if (CommonUtilities.isEmpty(accountIds)) {
      return 0;
    }

    String sql =
        "DELETE FROM "
            + tableName()
            + " WHERE account_id IN ("
            + DaoUtils.placeholders(accountIds.size())
            + ")";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, accountIds, Boolean.FALSE);
      return preparedStatement.executeUpdate();
    }
  }
}
