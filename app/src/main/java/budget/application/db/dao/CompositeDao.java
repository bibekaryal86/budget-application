package budget.application.db.dao;

import budget.application.db.util.DaoUtils;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeDao {
  private static final Logger log = LoggerFactory.getLogger(CompositeDao.class);

  private final Connection connection;
  private final String requestId;

  public CompositeDao(String requestId, Connection connection) {
    this.connection = connection;
    this.requestId = requestId;
  }

  public List<CompositeResponse.CategoryComposite> compositeCategories(CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite Categories Request=[{}]", requestId, cr);

    CompositeRequest.CategoryComposite crcc = cr == null ? null : cr.categoryComposite();
    List<UUID> catTypeIds = (crcc == null ? List.of() : crcc.catTypesId());

    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    c.id   AS category_id,
                    c.name AS category_name,

                    ct.id  AS category_type_id,
                    ct.name AS category_type_name

                FROM category c
                JOIN category_type ct
                  ON ct.id = c.category_type_id
                """);

    if (!CommonUtilities.isEmpty(catTypeIds)) {
      sql.append(" WHERE ct.id IN (").append(DaoUtils.placeholders(catTypeIds.size())).append(") ");
    }

    sql.append(" ORDER BY ct.name, c.name ASC ");

    String finalSql = sql.toString();
    log.debug("[{}] Composite Categories SQL=[{}]", requestId, finalSql);

    PreparedStatement stmt = connection.prepareStatement(finalSql);
    if (!CommonUtilities.isEmpty(catTypeIds)) {
      DaoUtils.bindParams(stmt, catTypeIds);
    }

    List<CompositeResponse.CategoryComposite> results = new ArrayList<>();

    try (ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        CompositeResponse.CategoryTypeComposite ct =
            new CompositeResponse.CategoryTypeComposite(
                rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name"));

        CompositeResponse.CategoryComposite c =
            new CompositeResponse.CategoryComposite(
                rs.getObject("category_id", UUID.class), rs.getString("category_name"), ct);

        results.add(c);
      }
    }

    return results;
  }

  public List<CompositeResponse.TransactionComposite> compositeTransactions(CompositeRequest cr)
      throws SQLException {

    log.debug("[{}] Composite Transactions Request=[{}]", requestId, cr);

    CompositeRequest.TransactionComposite crtc = cr.transactionComposite();

    List<UUID> catIds = crtc.catIds();
    List<UUID> catTypeIds = crtc.catTypeIds();
    LocalDate beginDate = crtc.beginDate();
    LocalDate endDate = crtc.endDate();
    List<String> merchants = crtc.merchants();
    List<String> txnTypes = crtc.txnTypes();

    StringBuilder sql =
        new StringBuilder(
            """
                          SELECT
                              t.id           AS txn_id,
                              t.txn_date     AS txn_date,
                              t.merchant     AS txn_merchant,
                              t.total_amount AS txn_total_amount,
                              t.notes        AS txn_notes,

                              ti.id          AS item_id,
                              ti.label       AS item_label,
                              ti.amount      AS item_amount,
                              ti.txn_type    AS item_txn_type,

                              c.id           AS category_id,
                              c.name         AS category_name,

                              ct.id          AS category_type_id,
                              ct.name        AS category_type_name

                          FROM transaction t
                          LEFT JOIN transaction_item ti
                                 ON ti.transaction_id = t.id
                          LEFT JOIN category c
                                 ON c.id = ti.category_id
                          LEFT JOIN category_type ct
                                 ON ct.id = c.category_type_id
                          """);

    List<Object> params = new ArrayList<>();
    final boolean[] whereAdded = {false};

    Consumer<String> addWhere =
        (condition) -> {
          sql.append(whereAdded[0] ? " AND " : " WHERE ");
          sql.append(condition);
          whereAdded[0] = true;
        };

    if (beginDate != null && endDate != null) {
      addWhere.accept("t.txn_date >= ? AND t.txn_date <= ?");
      params.add(beginDate);
      params.add(endDate);
    }

    if (!CommonUtilities.isEmpty(merchants)) {
      addWhere.accept("t.merchant IN (" + DaoUtils.placeholders(merchants.size()) + ")");
      params.addAll(merchants);
    }

    if (!CommonUtilities.isEmpty(catIds)) {
      addWhere.accept("ti.category_id IN (" + DaoUtils.placeholders(catIds.size()) + ")");
      params.addAll(catIds);
    }

    if (!CommonUtilities.isEmpty(catTypeIds)) {
      addWhere.accept("c.category_type_id IN (" + DaoUtils.placeholders(catTypeIds.size()) + ")");
      params.addAll(catTypeIds);
    }

    if (!CommonUtilities.isEmpty(txnTypes)) {
      addWhere.accept("ti.txn_type IN (" + DaoUtils.placeholders(txnTypes.size()) + ")");
      params.addAll(txnTypes);
    }

    sql.append(" ORDER BY t.txn_date DESC ");

    String finalSql = sql.toString();
    log.debug("[{}] Composite Transactions SQL=[{}]", requestId, finalSql);

    PreparedStatement stmt = connection.prepareStatement(finalSql);
    DaoUtils.bindParams(stmt, params);

    Map<UUID, TransactionCompositeBuilder> txnMap = new LinkedHashMap<>();

    try (ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {

        UUID txnId = rs.getObject("txn_id", UUID.class);

        txnMap.putIfAbsent(
            txnId,
            new TransactionCompositeBuilder(
                txnId,
                rs.getDate("txn_date").toLocalDate(),
                rs.getString("txn_merchant"),
                rs.getDouble("txn_total_amount"),
                rs.getString("txn_notes")));

        UUID itemId = rs.getObject("item_id", UUID.class);
        if (itemId != null) {

          CompositeResponse.CategoryTypeComposite ct =
              new CompositeResponse.CategoryTypeComposite(
                  rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name"));

          CompositeResponse.CategoryComposite c =
              new CompositeResponse.CategoryComposite(
                  rs.getObject("category_id", UUID.class), rs.getString("category_name"), ct);

          CompositeResponse.TransactionItemComposite item =
              new CompositeResponse.TransactionItemComposite(
                  itemId,
                  rs.getString("item_label"),
                  rs.getDouble("item_amount"),
                  rs.getString("item_txn_type"),
                  c);

          txnMap.get(txnId).addItem(item);
        }
      }
    }

    return txnMap.values().stream().map(TransactionCompositeBuilder::build).toList();
  }

  private static class TransactionCompositeBuilder {
    private final UUID id;
    private final LocalDate txnDate;
    private final String merchant;
    private final double totalAmount;
    private final String notes;
    private final List<CompositeResponse.TransactionItemComposite> items = new ArrayList<>();

    TransactionCompositeBuilder(
        UUID id, LocalDate txnDate, String merchant, double totalAmount, String notes) {
      this.id = id;
      this.txnDate = txnDate;
      this.merchant = merchant;
      this.totalAmount = totalAmount;
      this.notes = notes;
    }

    void addItem(CompositeResponse.TransactionItemComposite item) {
      items.add(item);
    }

    CompositeResponse.TransactionComposite build() {
      return new CompositeResponse.TransactionComposite(
          id, txnDate, merchant, totalAmount, notes, List.copyOf(items));
    }
  }
}
