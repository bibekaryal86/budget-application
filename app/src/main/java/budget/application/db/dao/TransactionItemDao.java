package budget.application.db.dao;

import budget.application.db.mapper.TransactionItemRowMapper;
import budget.application.db.util.DaoUtils;
import budget.application.model.entity.TransactionItem;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
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
    return List.of("transaction_id", "category_id", "label", "amount");
  }

  @Override
  protected List<Object> insertValues(TransactionItem ti) {
    return List.of(ti.transactionId(), ti.categoryId(), ti.label(), ti.amount());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_id", "label", "amount");
  }

  @Override
  protected List<Object> updateValues(TransactionItem ti) {
    return List.of(ti.categoryId(), ti.label(), ti.amount());
  }

  @Override
  protected UUID getId(TransactionItem ti) {
    return ti.id();
  }

    @Override
    protected String orderByClause() {
        return "transaction_id ASC";
    }

    // --- Custom ---
  public List<TransactionItem> readByTransactionIds(List<UUID> txnIds) throws SQLException {
    if (CommonUtilities.isEmpty(txnIds)) {
      return List.of();
    }

    String sql =
        "SELECT * FROM "
            + tableName()
            + " WHERE transaction_id IN ("
            + DaoUtils.placeholders(txnIds.size())
            + ")";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, txnIds);

      try (ResultSet rs = stmt.executeQuery()) {
        List<TransactionItem> results = new ArrayList<>();
        while (rs.next()) {
          results.add(mapper.map(rs));
        }
        return results;
      }
    }
  }

  public int deleteByTransactionIds(List<UUID> txnIds) throws SQLException {
    if (CommonUtilities.isEmpty(txnIds)) {
      return 0;
    }

    String sql =
        "DELETE FROM "
            + tableName()
            + " WHERE transaction_id IN ("
            + DaoUtils.placeholders(txnIds.size())
            + ")";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, txnIds);
      return stmt.executeUpdate();
    }
  }
}
