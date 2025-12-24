package budget.application.db.dao;

import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
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

public class CompositeDao {

  private final Connection connection;

  public CompositeDao(Connection connection) {
    this.connection = connection;
  }

  public List<CompositeResponse.TransactionComposite> read(CompositeRequest cr)
      throws SQLException {
    CompositeRequest.TransactionRequest req = cr.transactionRequest();

    String sql =
        """
            SELECT
                t.id                  AS txn_id,
                t.txn_date            AS txn_date,
                t.merchant            AS txn_merchant,
                t.total_amount        AS txn_total_amount,
                t.notes               AS txn_notes,

                ti.id                 AS item_id,
                ti.label              AS item_label,
                ti.amount             AS item_amount,
                ti.category_id        AS item_category_id,

                c.id                  AS category_id,
                c.name                AS category_name,
                c.category_type_id    AS category_type_id,

                ct.id                 AS category_type_id,
                ct.name               AS category_type_name

            FROM transaction t
            LEFT JOIN transaction_item ti
                   ON ti.transaction_id = t.id
            LEFT JOIN category c
                   ON c.id = ti.category_id
            LEFT JOIN category_type ct
                   ON ct.id = c.category_type_id

            WHERE 1 = 1
              AND (:merchant IS NULL OR t.merchant = :merchant)
              AND (:categoryId IS NULL OR ti.category_id = :categoryId)
              AND (:categoryTypeId IS NULL OR c.category_type_id = :categoryTypeId)
              AND (
                    (:beginDate IS NULL OR :endDate IS NULL)
                    OR (t.txn_date >= :beginDate AND t.txn_date <= :endDate)
                  )

            ORDER BY t.txn_date DESC, t.id, ti.id
            """;

    PreparedStatement stmt = connection.prepareStatement(sql);
    stmt.setObject(1, req.merchant());
    stmt.setObject(2, req.categoryId());
    stmt.setObject(3, req.categoryTypeId());
    stmt.setObject(4, req.beginDate());
    stmt.setObject(5, req.endDate());

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
                  itemId, rs.getDouble("item_amount"), c);

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
