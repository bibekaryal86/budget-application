package budget.application.db.dao;

import budget.application.db.mapper.TransactionRowMapper;
import budget.application.model.dto.PaginationResponse;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.entity.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDao extends BaseDao<Transaction> {

  public TransactionDao(String requestId, Connection connection) {
    super(requestId, connection, new TransactionRowMapper());
  }

  @Override
  protected String tableName() {
    return "transaction";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("txn_date", "merchant", "total_amount", "notes");
  }

  @Override
  protected List<Object> insertValues(Transaction t) {
    return List.of(
        t.txnDate(), t.merchant().toUpperCase(), t.totalAmount(), t.notes().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("txn_date", "merchant", "total_amount", "notes", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Transaction t) {
    return List.of(
        t.txnDate(),
        t.merchant().toUpperCase(),
        t.totalAmount(),
        t.notes().toUpperCase(),
        LocalDateTime.now());
  }

  @Override
  protected UUID getId(Transaction t) {
    return t.id();
  }

  @Override
  protected String orderByClause() {
    return "txn_date DESC";
  }

  public List<Transaction> readAllMerchants() throws SQLException {
    String sql = "SELECT DISTINCT merchant FROM transaction ORDER BY merchant ASC";
    List<Transaction> items = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        items.add(new Transaction(null, null, rs.getString("merchant"), 0.0, null, null, null));
      }
    }
    return items;
  }

  public PaginationResponse<Transaction> readAll(PaginationRequest pr) throws SQLException {
    log.debug("[{}] Read All Transactions PaginationRequest=[{}]", requestId, pr);
    String sql =
        """
        SELECT *
        FROM transaction
        ORDER BY txn_date DESC
        LIMIT ? OFFSET ?
    """;
    int pageNumber = pr.pageNumber() == 0 ? 1 : pr.pageNumber();
    int perPage = pr.perPage() == 0 ? 1000 : pr.perPage();
    int offset = (pageNumber - 1) * perPage;
    int limit = perPage;

    List<Transaction> items = new ArrayList<>();

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, limit);
      stmt.setInt(2, offset);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          items.add(mapper.map(rs));
        }
      }
    }

    return new PaginationResponse<>(items, countAll(), pageNumber, perPage);
  }

  private int countAll() throws SQLException {
    String sql = "SELECT COUNT(*) FROM transaction";

    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      rs.next();
      return rs.getInt(1);
    }
  }
}
