package budget.application.db.dao;

import budget.application.db.mapper.TransactionItemRowMapper;
import budget.application.model.entities.TransactionItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionItemDao extends BaseDao<TransactionItem> {

  public TransactionItemDao(Connection connection) {
    super(connection, new TransactionItemRowMapper());
  }

  @Override
  protected String tableName() {
    return "transaction_item";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("id", "transaction_id", "category_id", "label", "amount");
  }

  @Override
  protected List<Object> insertValues(TransactionItem ti) {
    return List.of(ti.id(), ti.transactionId(), ti.categoryId(), ti.label(), ti.amount());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("transaction_id", "category_id", "label", "amount");
  }

  @Override
  protected List<Object> updateValues(TransactionItem ti) {
    return List.of(ti.transactionId(), ti.categoryId(), ti.label(), ti.amount());
  }

  @Override
  protected UUID getId(TransactionItem ti) {
    return ti.id();
  }

  // --- Custom ---
  public List<TransactionItem> readByTransactionId(UUID transactionId) throws SQLException {
    String sql = "SELECT * FROM " + tableName() + " WHERE transaction_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setObject(1, transactionId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<TransactionItem> results = new ArrayList<>();
        while (rs.next()) {
          results.add(mapper.map(rs));
        }
        return results;
      }
    }
  }
}
