package budget.application.db.mapper;

import budget.application.model.dto.AccountResponse;
import budget.application.model.entity.Account;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountRowMappers {
  public static class AccountRowMapper implements RowMapper<Account> {
    @Override
    public Account map(ResultSet resultSet) throws SQLException {
      return new Account(
          resultSet.getObject("id", UUID.class),
          resultSet.getString("name"),
          resultSet.getString("account_type"),
          resultSet.getString("bank_name"),
          resultSet.getBigDecimal("opening_balance"),
          resultSet.getString("status"),
          resultSet.getObject("created_at", LocalDateTime.class),
          resultSet.getObject("updated_at", LocalDateTime.class));
    }
  }

  public static class AccountRowMapperResponse implements RowMapper<AccountResponse.Account> {
    @Override
    public AccountResponse.Account map(ResultSet resultSet) throws SQLException {
      return new AccountResponse.Account(
          resultSet.getObject("id", UUID.class),
          resultSet.getString("name"),
          resultSet.getString("account_type"),
          resultSet.getString("bank_name"),
          resultSet.getBigDecimal("opening_balance"),
          resultSet.getString("status"));
    }
  }
}
