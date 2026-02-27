package budget.application.db.dao;

import budget.application.db.mapper.AccountRowMappers;
import budget.application.model.entity.Account;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountDao extends BaseDao<Account> {

  public AccountDao(Connection connection) {
    super(connection, new AccountRowMappers.AccountRowMapper(), null);
  }

  @Override
  protected String tableName() {
    return "account";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("name", "account_type", "bank_name", "status");
  }

  @Override
  protected List<Object> insertValues(Account account) {
    return List.of(
        account.name().toUpperCase(), account.accountType(), account.bankName(), account.status());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name", "account_type", "bank_name", "status");
  }

  @Override
  protected List<Object> updateValues(Account account) {
    return List.of(
        account.name().toUpperCase(), account.accountType(), account.bankName(), account.status());
  }

  @Override
  protected UUID getId(Account account) {
    return account.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public List<Account> readNoEx(List<UUID> ids) {
    try {
      return read(ids);
    } catch (Exception e) {
      return List.of();
    }
  }

  public List<String> readAllBanks() throws SQLException {
    String sql = "SELECT DISTINCT bank_name FROM account ORDER BY bank_name ASC";
    List<String> bankNames = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        bankNames.add(resultSet.getString("bank_name"));
      }
    }
    return bankNames;
  }

  public int updateAccountBalances(Map<UUID, BigDecimal> accountBalanceUpdates)
      throws SQLException {
    String sql =
        "WITH data(id, balance) AS ("
            + "  SELECT UNNEST(?::uuid[]), UNNEST(?::numeric[])"
            + ")"
            + "UPDATE account a "
            + "SET account_balance = d.balance "
            + "FROM data d "
            + "WHERE a.id = d.id "
            + "RETURNING a.*";

    boolean originalAutoCommit = connection.getAutoCommit();
    int updatedCount = 0;

    try {
      connection.setAutoCommit(false);

      UUID[] ids = accountBalanceUpdates.keySet().toArray(UUID[]::new);
      BigDecimal[] balances = accountBalanceUpdates.values().toArray(BigDecimal[]::new);

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setArray(1, connection.createArrayOf("uuid", ids));
        preparedStatement.setArray(2, connection.createArrayOf("numeric", balances));
        updatedCount = preparedStatement.executeUpdate();
      }

      connection.commit();
      return updatedCount;
    } catch (SQLException e) {
      connection.rollback();
      throw e;
    } finally {
      connection.setAutoCommit(originalAutoCommit);
    }
  }
}
