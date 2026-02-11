package budget.application.db.dao;

import budget.application.cache.AccountCache;
import budget.application.db.mapper.AccountRowMappers;
import budget.application.model.entity.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountDao extends BaseDao<Account> {

  public AccountDao(Connection connection, AccountCache accountCache) {
    super(connection, new AccountRowMappers.AccountRowMapper(), accountCache);
  }

  @Override
  protected String tableName() {
    return "account";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("name", "account_type", "bank_name", "opening_balance", "status");
  }

  @Override
  protected List<Object> insertValues(Account account) {
    return List.of(
        account.name().toUpperCase(),
        account.accountType(),
        account.bankName(),
        account.openingBalance(),
        account.status());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name", "account_type", "bank_name", "opening_balance", "status");
  }

  @Override
  protected List<Object> updateValues(Account account) {
    return List.of(
        account.name().toUpperCase(),
        account.accountType(),
        account.bankName(),
        account.openingBalance(),
        account.status());
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
}
