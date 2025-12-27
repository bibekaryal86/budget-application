package budget.application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public final class TestDataHelper {

  private final DataSource ds;

  public TestDataHelper(DataSource ds) {
    this.ds = ds;
  }

  public UUID insertTransaction(UUID id, LocalDate txnDate, double totalAmount)
      throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                INSERT INTO transaction (id, txn_date, merchant, total_amount)
                VALUES (?, ?, ?, ?)
            """)) {

      stmt.setObject(1, id);
      stmt.setObject(2, txnDate);
      stmt.setString(3, "Merchant: " + id);
      stmt.setDouble(4, totalAmount);
      stmt.executeUpdate();
    }
    return id;
  }

  public UUID insertTransactionItem(UUID id, UUID txnId, double amount) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                INSERT INTO transaction_item (id, transaction_id, category_id, label, amount)
                VALUES (?, ?, ?, ?, ?)
            """)) {
      stmt.setObject(1, id);
      stmt.setObject(2, txnId);
      stmt.setObject(3, IntegrationBaseTest.TEST_ID);
      stmt.setString(4, "Label: " + id);
      stmt.setDouble(5, amount);
      stmt.executeUpdate();
    }
    return id;
  }

  public List<UUID> insertBulkTransactions(
      int count,
      double baseTotalAmount,
      double baseItemAmount,
      boolean randomizeAmounts,
      double mismatchRatio, // e.g., 0.10 = 10% mismatches
      int minItemsPerTxn,
      int maxItemsPerTxn)
      throws SQLException {

    List<UUID> ids = new ArrayList<>(count);

    try (Connection c = ds.getConnection()) {
      c.setAutoCommit(false);

      for (int i = 0; i < count; i++) {

        UUID txnId = UUID.randomUUID();
        ids.add(txnId);

        double totalAmount = baseTotalAmount;
        double itemAmount = baseItemAmount;

        if (randomizeAmounts) {
          totalAmount = baseTotalAmount + (Math.random() * 50 - 25);
          itemAmount = baseItemAmount + (Math.random() * 20 - 10);
        }

        boolean isMismatch = Math.random() < mismatchRatio;

        if (isMismatch) {
          itemAmount = itemAmount / 2.0;
        }

        int itemCount = minItemsPerTxn;
        if (maxItemsPerTxn > minItemsPerTxn) {
          itemCount =
              minItemsPerTxn + (int) (Math.random() * (maxItemsPerTxn - minItemsPerTxn + 1));
        }

        insertTransaction(txnId, LocalDate.of(2024, 1, 1), totalAmount);

        for (int j = 0; j < itemCount; j++) {
          insertTransactionItem(UUID.randomUUID(), txnId, itemAmount);
        }

        if (i % 250 == 0) {
          c.commit();
        }
      }

      c.commit();
    }

    return ids;
  }

  public void deleteBulkTransactions(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      c.setAutoCommit(false);

      try (PreparedStatement deleteItems = c.prepareStatement("DELETE FROM transaction_item")) {
        deleteItems.executeUpdate();
      }

      if (keepIds == null || keepIds.isEmpty()) {
        try (PreparedStatement deleteTxns = c.prepareStatement("DELETE FROM transaction")) {
          deleteTxns.executeUpdate();
        }
      } else {
        String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql = "DELETE FROM transaction WHERE id NOT IN (" + placeholders + ")";

        try (PreparedStatement deleteTxns = c.prepareStatement(sql)) {
          for (int i = 0; i < keepIds.size(); i++) {
            deleteTxns.setObject(i + 1, keepIds.get(i));
          }
          deleteTxns.executeUpdate();
        }
      }

      c.commit();
    }
  }
}
