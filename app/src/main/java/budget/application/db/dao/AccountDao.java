package budget.application.db.dao;

import budget.application.db.mapper.AccountRowMappers;
import budget.application.model.entity.Account;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class AccountDao extends BaseDao<Account> {

  public AccountDao(String requestId, Connection connection) {
    super(requestId, connection, new AccountRowMappers.AccountRowMapper());
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
  protected List<Object> insertValues(Account acc) {
    return List.of(
        acc.name().toUpperCase(),
        acc.accountType(),
        acc.bankName(),
        acc.openingBalance(),
        acc.status());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("name", "account_type", "bank_name", "opening_balance", "status");
  }

  @Override
  protected List<Object> updateValues(Account acc) {
    return List.of(
        acc.name().toUpperCase(),
        acc.accountType(),
        acc.bankName(),
        acc.openingBalance(),
        acc.status());
  }

  @Override
  protected UUID getId(Account acc) {
    return acc.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }
}
