package budget.application.db.mapper;

import budget.application.model.entity.Transaction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionRowMapper implements RowMapper<Transaction> {
  @Override
  public Transaction map(ResultSet rs) throws SQLException {
    return new Transaction(
        rs.getObject("id", UUID.class),
        rs.getObject("txn_date", LocalDateTime.class),
        rs.getString("merchant"),
        rs.getDouble("total_amount"),
        rs.getString("notes"),
        rs.getObject("created_at", LocalDateTime.class),
        rs.getObject("updated_at", LocalDateTime.class));
  }
}
