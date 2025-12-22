package budget.application.db.mapper;

import budget.application.model.entity.Transaction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionRowMapper implements RowMapper<Transaction> {
  @Override
  public Transaction map(ResultSet rs) throws SQLException {
    return Transaction.builder()
        .id(rs.getObject("id", UUID.class))
        .txnDate(rs.getObject("txn_date", LocalDateTime.class))
        .merchant(rs.getString("merchant"))
        .totalAmount(rs.getDouble("total_amount"))
        .notes(rs.getString("notes"))
        .createdAt(rs.getObject("created_at", LocalDateTime.class))
        .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
        .build();
  }
}
