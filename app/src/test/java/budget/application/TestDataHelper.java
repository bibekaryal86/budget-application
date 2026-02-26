package budget.application;

import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public final class TestDataHelper {

  private final DataSource ds;

  public TestDataHelper(DataSource ds) {
    this.ds = ds;
  }

  public UUID insertCategoryType(UUID id, String name) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                           INSERT INTO category_type (id, name)
                           VALUES (?, ?)
                       """)) {

      stmt.setObject(1, id);
      stmt.setObject(2, name);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteCategoryType(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM category_type")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM category_type WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public UUID insertCategory(UUID id, UUID catTypeId, String name) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                             INSERT INTO category (id, category_type_id, name)
                             VALUES (?, ?, ?)
                         """)) {

      stmt.setObject(1, id);
      stmt.setObject(2, catTypeId);
      stmt.setObject(3, name);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteCategory(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM category")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM category WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public UUID insertAccount(UUID id, String name) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                                          INSERT INTO account (id, name, account_type, bank_name, opening_balance, status)
                                          VALUES (?, ?, 'CHECKING', 'TEST BANK', 1000.00, 'ACTIVE')
                                      """)) {

      stmt.setObject(1, id);
      stmt.setString(2, name);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteAccount(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM account")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM account WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public UUID insertBudget(UUID id, UUID catId, int month, int year) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                                                       INSERT INTO budget (id, category_id, budget_month, budget_year, amount, notes)
                                                       VALUES (?, ?, ?, ?, 1000.00, 'notes example')
                                                   """)) {

      stmt.setObject(1, id);
      stmt.setObject(2, catId);
      stmt.setInt(3, month);
      stmt.setInt(4, year);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteBudget(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM budget")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM budget WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public UUID insertTransaction(UUID id, LocalDateTime txnDate, double totalAmount)
      throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                INSERT INTO transaction (id, txn_date, merchant, total_amount)
                VALUES (?, ?, ?, ?)
            """)) {

      stmt.setObject(1, id);
      stmt.setObject(2, txnDate.toLocalDate());
      stmt.setString(3, "Merchant: " + id);
      stmt.setDouble(4, totalAmount);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteTransaction(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM transaction")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM transaction WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public UUID insertTransactionItem(
      UUID id, UUID txnId, UUID catId, double amount, List<String> tags) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement stmt =
            c.prepareStatement(
                """
                INSERT INTO transaction_item (id, transaction_id, category_id, account_id, amount, tags, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """)) {
      stmt.setObject(1, id);
      stmt.setObject(2, txnId);
      stmt.setObject(3, catId);
      stmt.setObject(4, IntegrationBaseTest.TEST_ID);
      stmt.setDouble(5, amount);
      stmt.setArray(6, c.createArrayOf("text", tags.toArray(new String[0])));
      stmt.setString(7, "Note: " + id);
      stmt.executeUpdate();
    }
    return id;
  }

  public void deleteTransactionItem(List<UUID> keepIds) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement stmt = c.prepareStatement("DELETE FROM transaction_item")) {
          stmt.executeUpdate();
        }
        return;
      }

      String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
      String sql = "DELETE FROM transaction_item WHERE id NOT IN (" + placeholders + ")";

      try (PreparedStatement stmt = c.prepareStatement(sql)) {
        for (int i = 0; i < keepIds.size(); i++) {
          stmt.setObject(i + 1, keepIds.get(i));
        }
        stmt.executeUpdate();
      }
    }
  }

  public List<UUID> insertBulkTransactions(
      int count,
      double baseTotalAmount,
      double baseItemAmount,
      boolean randomizeAmounts,
      double mismatchRatio,
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

        int itemCount = minItemsPerTxn;
        if (maxItemsPerTxn > minItemsPerTxn) {
          itemCount =
              minItemsPerTxn + (int) (Math.random() * (maxItemsPerTxn - minItemsPerTxn + 1));
        }

        if (!isMismatch) {
          totalAmount = itemAmount * itemCount;
        } else {
          totalAmount = itemAmount * itemCount + 1.0;
        }

        insertTransaction(txnId, LocalDateTime.of(2024, 1, 1, 0, 0, 0), totalAmount);

        for (int j = 0; j < itemCount; j++) {
          insertTransactionItem(
              UUID.randomUUID(),
              txnId,
              IntegrationBaseTest.TEST_ID,
              itemAmount,
              Collections.emptyList());
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

      if (CommonUtilities.isEmpty(keepIds)) {
        try (PreparedStatement deleteItems = c.prepareStatement("DELETE FROM transaction_item")) {
          deleteItems.executeUpdate();
        }
        try (PreparedStatement deleteTxns = c.prepareStatement("DELETE FROM transaction")) {
          deleteTxns.executeUpdate();
        }
      } else {
        String placeholders = keepIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String itemsSql = "DELETE FROM transaction WHERE id NOT IN (" + placeholders + ")";
        String txnsSql = "DELETE FROM transaction WHERE id NOT IN (" + placeholders + ")";

        try (PreparedStatement deleteItems = c.prepareStatement(itemsSql)) {
          for (int i = 0; i < keepIds.size(); i++) {
            deleteItems.setObject(i + 1, keepIds.get(i));
          }
          deleteItems.executeUpdate();
        }

        try (PreparedStatement deleteTxns = c.prepareStatement(txnsSql)) {
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
