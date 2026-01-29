package budget.application.db.dao;

import budget.application.db.mapper.AccountRowMappers;
import budget.application.model.entity.Account;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class AccountDao extends BaseDao<Account> {

  public AccountDao(Connection connection) {
    super(connection, new AccountRowMappers.AccountRowMapper());
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
}
