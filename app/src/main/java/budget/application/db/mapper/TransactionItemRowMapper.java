package budget.application.db.mapper;

import budget.application.model.entity.TransactionItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionItemRowMapper implements RowMapper<TransactionItem> {
  @Override
  public TransactionItem map(ResultSet rs) throws SQLException {
    return new TransactionItem(
        rs.getObject("id", UUID.class),
        rs.getObject("transaction_id", UUID.class),
        rs.getObject("category_id", UUID.class),
        rs.getString("label"),
        rs.getDouble("amount"),
        rs.getString("txn_type"));
  }
}
