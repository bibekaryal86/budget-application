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
    public Transaction map(ResultSet rs) throws SQLException {
      return new Transaction(
          rs.getObject("id", UUID.class),
          rs.getObject("txn_date", LocalDateTime.class),
          rs.getString("merchant"),
          rs.getObject("account_id", UUID.class),
          rs.getDouble("total_amount"),
          rs.getString("notes"),
          rs.getObject("created_at", LocalDateTime.class),
          rs.getObject("updated_at", LocalDateTime.class));
    }
  }

  @Deprecated
  public static class TransactionRowMapperResponse
      implements RowMapper<TransactionResponse.Transaction> {
    @Override
    public TransactionResponse.Transaction map(ResultSet rs) throws SQLException {
      return new TransactionResponse.Transaction(
          rs.getObject("txn_id", UUID.class),
          rs.getObject("txn_date", LocalDateTime.class),
          rs.getString("txn_merchant"),
          rs.getDouble("txn_total_amount"),
          rs.getString("txn_notes"),
          null,
          List.of());
    }
  }
}
