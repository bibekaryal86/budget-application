package budget.application.db.dao;

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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class TransactionItemDao extends BaseDao<TransactionItem> {

  private final TransactionItemRowMappers.TransactionItemRowMapperResponse
      transactionItemRowMapperResponse;

  public TransactionItemDao(String requestId, Connection connection) {
    super(requestId, connection, new TransactionItemRowMappers.TransactionItemRowMapper());
    this.transactionItemRowMapperResponse =
        new TransactionItemRowMappers.TransactionItemRowMapperResponse();
  }

  @Override
  protected String tableName() {
    return "transaction_item";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("transaction_id", "category_id", "amount", "tags", "notes");
  }

  @Override
  protected List<Object> insertValues(TransactionItem transactionItem) {
    return List.of(
        transactionItem.transactionId(),
        transactionItem.categoryId(),
        transactionItem.amount(),
        switch (transactionItem.tags()) {
          case null -> Collections.emptyList();
          case List<String> tags -> tags.stream().map(String::toUpperCase).toList();
        },
        transactionItem.notes().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_id", "amount", "tags", "notes");
  }

  @Override
  protected List<Object> updateValues(TransactionItem transactionItem) {
    return List.of(
        transactionItem.categoryId(),
        transactionItem.amount(),
        switch (transactionItem.tags()) {
          case null -> Collections.emptyList();
          case List<String> tags -> tags.stream().map(String::toUpperCase).toList();
        },
        transactionItem.notes().toUpperCase());
  }

  @Override
  protected UUID getId(TransactionItem transactionItem) {
    return transactionItem.id();
  }

  @Override
  protected String orderByClause() {
    return "transaction_id ASC";
  }

  // --- Custom ---
  public List<TransactionItem> createItems(List<TransactionItem> transactionItemsIn)
      throws SQLException {
    List<TransactionItem> transactionItemsOut = new ArrayList<>();
    for (TransactionItem transactionItem : transactionItemsIn) {
      transactionItemsOut.add(create(transactionItem));
    }
    return transactionItemsOut;
  }

  public List<TransactionItemResponse.TransactionItem> readTransactionItems(
      List<UUID> transactionItemIds) throws SQLException {
    log.debug("[{}] Read Transaction Items: TransactionItemIds={}", requestId, transactionItemIds);

    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    ti.id AS txn_item_id,
                    ti.amount AS txn_item_amount,
                    ti.tags AS txn_item_tags,
                    ti.notes AS txn_item_notes,
                    t.id AS txn_id,
                    t.txn_date AS txn_date,
                    t.merchant AS txn_merchant,
                    t.total_amount AS txn_total_amount,
                    c.id AS category_id,
                    c.name AS category_name,
                    ct.id AS category_type_id,
                    ct.name AS category_type_name
                FROM transaction_item ti
                LEFT JOIN transaction t
                  ON ti.transaction_id = t.id
                LEFT JOIN category c
                  ON ti.category_id = c.id
                LEFT JOIN category_type ct
                  ON c.category_type_id = ct.id
                """);

    List<Object> params = new ArrayList<>();
    final boolean[] whereAdded = {false};

    Consumer<String> addWhere =
        (condition) -> {
          sql.append(whereAdded[0] ? " AND " : " WHERE ");
          sql.append(condition);
          whereAdded[0] = true;
        };

    if (!CommonUtilities.isEmpty(transactionItemIds)) {
      addWhere.accept("ti.id IN (" + DaoUtils.placeholders(transactionItemIds.size()) + ")");
      params.addAll(transactionItemIds);
    }

    sql.append(" ORDER BY ti.transaction_id ASC, t.txn_date DESC ");

    log.debug("[{}] Read Transaction Items SQL=[{}]", requestId, sql);

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      }
      List<TransactionItemResponse.TransactionItem> results = new ArrayList<>();
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(transactionItemRowMapperResponse.map(resultSet));
        }
        return results;
      }
    }
  }

  public List<TransactionItem> readByTransactionIds(List<UUID> transactionIds) throws SQLException {
    log.debug("[{}] Reading transaction items for TransactionIds: {}", requestId, transactionIds);
    if (CommonUtilities.isEmpty(transactionIds)) {
      return List.of();
    }

    String sql =
        "SELECT * FROM "
            + tableName()
            + " WHERE transaction_id IN ("
            + DaoUtils.placeholders(transactionIds.size())
            + ")";
    log.debug("[{}] Read By Transaction Ids SQL=[{}]", requestId, sql);

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, transactionIds, Boolean.TRUE);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        List<TransactionItem> results = new ArrayList<>();
        while (resultSet.next()) {
          results.add(mapper.map(resultSet));
        }
        return results;
      }
    }
  }

  public List<String> readAllTags() throws SQLException {
    String sql =
        """
            SELECT DISTINCT tag
            FROM transaction_item
            CROSS JOIN LATERAL unnest(tags) AS tag
            WHERE tags IS NOT NULL AND cardinality(tags) > 0
            ORDER BY tag ASC
        """;

    List<String> tags = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        tags.add(resultSet.getString("tag"));
      }
    }
    return tags;
  }

  public int deleteByTransactionIds(List<UUID> transactionIds) throws SQLException {
    if (CommonUtilities.isEmpty(transactionIds)) {
      return 0;
    }

    String sql =
        "DELETE FROM "
            + tableName()
            + " WHERE transaction_id IN ("
            + DaoUtils.placeholders(transactionIds.size())
            + ")";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, transactionIds, Boolean.FALSE);
      return preparedStatement.executeUpdate();
    }
  }
}
