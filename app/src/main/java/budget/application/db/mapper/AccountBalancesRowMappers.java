package budget.application.db.mapper;

import budget.application.model.entity.AccountBalances;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountBalancesRowMappers {
  public static class AccountBalancesRowMapper implements RowMapper<AccountBalances> {
    @Override
    public AccountBalances map(ResultSet resultSet) throws SQLException {
      return new AccountBalances(
          resultSet.getObject("id", UUID.class),
          resultSet.getObject("account_id", UUID.class),
          resultSet.getString("year_month"),
          resultSet.getBigDecimal("account_balance"),
          resultSet.getString("notes"),
          resultSet.getObject("created_at", LocalDateTime.class),
          resultSet.getObject("updated_at", LocalDateTime.class));
    }
  }
}
