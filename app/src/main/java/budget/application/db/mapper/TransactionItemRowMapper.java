package budget.application.db.mapper;

import budget.application.model.entity.TransactionItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionItemRowMapper implements RowMapper<TransactionItem> {
  @Override
  public TransactionItem map(ResultSet rs) throws SQLException {
    return TransactionItem.builder()
        .id(rs.getObject("id", UUID.class))
        .transactionId(rs.getObject("transaction_id", UUID.class))
        .categoryId(rs.getObject("category_id", UUID.class))
        .label(rs.getString("label"))
        .amount(rs.getDouble("amount"))
        .txnType(rs.getString("txn_type"))
        .build();
  }
}
