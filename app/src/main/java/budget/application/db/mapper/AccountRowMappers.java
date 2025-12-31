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
    public Account map(ResultSet rs) throws SQLException {
      return new Account(
          rs.getObject("id", UUID.class),
          rs.getString("name"),
          rs.getString("account_type"),
          rs.getString("bank_name"),
          rs.getDouble("opening_balance"),
          rs.getString("status"),
          rs.getObject("created_at", LocalDateTime.class),
          rs.getObject("updated_at", LocalDateTime.class));
    }
  }

  public static class AccountRowMapperResponse implements RowMapper<AccountResponse.Account> {
    @Override
    public AccountResponse.Account map(ResultSet rs) throws SQLException {
      return new AccountResponse.Account(
          rs.getObject("id", UUID.class),
          rs.getString("name"),
          rs.getString("account_type"),
          rs.getString("bank_name"),
          rs.getDouble("opening_balance"),
          rs.getString("status"));
    }
  }
}
