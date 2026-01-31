package budget.application.db.mapper;

import budget.application.model.dto.TransactionResponse;
import budget.application.model.entity.Transaction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionRowMappers {
  public static class TransactionRowMapper implements RowMapper<Transaction> {
    @Override
    public Transaction map(ResultSet resultSet) throws SQLException {
      return new Transaction(
          resultSet.getObject("id", UUID.class),
          resultSet.getObject("txn_date", LocalDateTime.class),
          resultSet.getString("merchant"),
          resultSet.getObject("account_id", UUID.class),
          resultSet.getBigDecimal("total_amount"),
          resultSet.getObject("created_at", LocalDateTime.class),
          resultSet.getObject("updated_at", LocalDateTime.class));
    }
  }

  @Deprecated
  public static class TransactionRowMapperResponse
      implements RowMapper<TransactionResponse.Transaction> {
    @Override
    public TransactionResponse.Transaction map(ResultSet resultSet) throws SQLException {
      return new TransactionResponse.Transaction(
          resultSet.getObject("txn_id", UUID.class),
          resultSet.getObject("txn_date", LocalDateTime.class),
          resultSet.getString("txn_merchant"),
          resultSet.getBigDecimal("txn_total_amount"),
          null,
          List.of());
    }
  }
}
