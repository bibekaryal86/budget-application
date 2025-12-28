package budget.application.db.dao;

import budget.application.db.mapper.RowMapper;
import budget.application.db.mapper.TransactionItemRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.TransactionItemResponse;
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

  private final RowMapper<TransactionItemResponse.TransactionItem> txnItemRespMapper;

  public TransactionItemDao(String requestId, Connection connection) {
    super(requestId, connection, new TransactionItemRowMappers.TransactionItemRowMapper());
    this.txnItemRespMapper = new TransactionItemRowMappers.TransactionItemRowMapperResponse();
  }

  @Override
  protected String tableName() {
    return "transaction_item";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("transaction_id", "category_id", "label", "amount", "txn_type");
  }

  @Override
  protected List<Object> insertValues(TransactionItem ti) {
    return List.of(
        ti.transactionId(), ti.categoryId(), ti.label().toUpperCase(), ti.amount(), ti.txnType());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_id", "label", "amount", "txn_type");
  }

  @Override
  protected List<Object> updateValues(TransactionItem ti) {
    return List.of(ti.categoryId(), ti.label().toUpperCase(), ti.amount(), ti.txnType());
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
  public List<TransactionItem> createItems(List<TransactionItem> itemsIn) throws SQLException {
    List<TransactionItem> itemsOut = new ArrayList<>();
    for (TransactionItem item : itemsIn) {
      itemsOut.add(create(item));
    }
    return itemsOut;
  }

  public List<TransactionItemResponse.TransactionItem> readTransactionItems(
      List<UUID> txnItemIds, List<UUID> txnIds, List<UUID> catIds, List<String> txnTypes)
      throws SQLException {
    log.debug(
        "[{}] Read Transaction Items: txnItemIds={}, txnIds={}, catIds={}, txnTypes={}",
        requestId,
        txnItemIds,
        txnIds,
        catIds,
        txnTypes);

    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    ti.id AS txn_item_id,
                    ti.label AS txn_item_label,
                    ti.amount AS txn_item_amount,
                    ti.txn_type AS txn_item_type,
                    t.id AS txn_id,
                    t.txn_date AS txn_date,
                    t.merchant AS txn_merchant,
                    t.total_amount AS txn_total_amount,
                    t.notes AS txn_notes,
                    c.id AS category_id,
                    c.name AS category_name,
                    ct.id AS category_type_id,
                    ct.name AS category_type_name
                FROM transaction_item ti
                JOIN transaction t
                  ON ti.transaction_id = t.id
                JOIN category c
                  ON ti.category_id = c.id
                JOIN category_type ct
                  ON c.category_type_id = ct.id
                """);

      boolean hasWhere = false;

    if (!CommonUtilities.isEmpty(txnItemIds)) {
      sql.append(" WHERE ti.id IN (").append(DaoUtils.placeholders(txnItemIds.size())).append(")");
        hasWhere = true;
    }
    if (!CommonUtilities.isEmpty(txnIds)) {
        sql.append(hasWhere ? " AND " : " WHERE ");
      sql.append(" ti.transaction_id IN (")
          .append(DaoUtils.placeholders(txnIds.size()))
          .append(")");
    }
    if (!CommonUtilities.isEmpty(catIds)) {
        sql.append(hasWhere ? " AND " : " WHERE ");
      sql.append(" c.category_type_id IN (")
          .append(DaoUtils.placeholders(catIds.size()))
          .append(")");
    }
    if (!CommonUtilities.isEmpty(txnTypes)) {
        sql.append(hasWhere ? " AND " : " WHERE ");
      sql.append(" ti.txn_type IN (").append(DaoUtils.placeholders(txnTypes.size())).append(")");
    }
    sql.append(" ORDER BY ti.transaction_id ASC, t.txn_date DESC ");

    log.debug("[{}] Read Transaction Items SQL=[{}]", requestId, sql);

    try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
      if (!CommonUtilities.isEmpty(txnItemIds)) {
        DaoUtils.bindParams(stmt, txnItemIds);
      }
      if (!CommonUtilities.isEmpty(txnIds)) {
        DaoUtils.bindParams(stmt, txnIds);
      }
      if (!CommonUtilities.isEmpty(catIds)) {
        DaoUtils.bindParams(stmt, catIds);
      }
      if (!CommonUtilities.isEmpty(txnTypes)) {
        DaoUtils.bindParams(stmt, txnTypes);
      }

      try (ResultSet rs = stmt.executeQuery()) {
        List<TransactionItemResponse.TransactionItem> results = new ArrayList<>();
        while (rs.next()) {
          results.add(txnItemRespMapper.map(rs));
        }
        return results;
      }
    }
  }

  public List<TransactionItem> readByTransactionIds(List<UUID> txnIds) throws SQLException {
    log.debug("[{}] Reading transaction items for txnIds: {}", requestId, txnIds);
    if (CommonUtilities.isEmpty(txnIds)) {
      return List.of();
    }

    String sql =
        "SELECT * FROM "
            + tableName()
            + " WHERE transaction_id IN ("
            + DaoUtils.placeholders(txnIds.size())
            + ")";
    log.debug("[{}] Read By Transaction Ids SQL=[{}]", requestId, sql);

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
