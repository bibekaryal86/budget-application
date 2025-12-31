package budget.application.db.dao;

import budget.application.db.mapper.TransactionRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.PaginationResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.dto.TransactionResponse;
import budget.application.model.entity.Transaction;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TransactionDao extends BaseDao<Transaction> {

  public TransactionDao(String requestId, Connection connection) {
    super(requestId, connection, new TransactionRowMappers.TransactionRowMapper());
  }

  @Override
  protected String tableName() {
    return "transaction";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("txn_date", "merchant", "account_id", "total_amount", "notes");
  }

  @Override
  protected List<Object> insertValues(Transaction t) {
    return List.of(
        t.txnDate().toLocalDate(),
        t.merchant().toUpperCase(),
        t.accountId(),
        t.totalAmount(),
        t.notes().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("txn_date", "merchant", "account_id", "total_amount", "notes", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Transaction t) {
    return List.of(
        t.txnDate().toLocalDate(),
        t.merchant().toUpperCase(),
        t.accountId(),
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

  public List<TransactionResponse.Transaction> readTransactions(
      List<UUID> txnIds, RequestParams.TransactionParams requestParams) throws SQLException {
    log.debug("[{}] Read Transactions: TxnIds=[{}], Params=[{}]", requestId, txnIds, requestParams);

    if (requestParams == null) {
      requestParams =
          new RequestParams.TransactionParams(
              null, null, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    List<UUID> catIds = requestParams.catIds();
    List<UUID> catTypeIds = requestParams.catTypeIds();
    LocalDate beginDate = requestParams.beginDate();
    LocalDate endDate = requestParams.endDate();
    List<String> merchants = requestParams.merchants();
    List<UUID> accIds = requestParams.accIds();
    List<String> expTypes = requestParams.expTypes();

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
                    ti.exp_type    AS item_exp_type,
                    c.id           AS category_id,
                    c.name         AS category_name,
                    ct.id          AS category_type_id,
                    ct.name        AS category_type_name,
                    a.id           AS account_id,
                    a.name         AS account_name,
                    a.account_type AS account_type,
                    a.bank_name    AS account_bank_name,
                    a.opening_balance AS account_opening_balance,
                    a.status       AS account_status
                FROM transaction t
                JOIN account a
                    ON t.account_id = a.id
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
    if (!CommonUtilities.isEmpty(txnIds)) {
      addWhere.accept("t.id IN (" + DaoUtils.placeholders(txnIds.size()) + ")");
      params.addAll(txnIds);
    }
    if (!CommonUtilities.isEmpty(merchants)) {
      addWhere.accept("t.merchant IN (" + DaoUtils.placeholders(merchants.size()) + ")");
      params.addAll(merchants);
    }
    if (!CommonUtilities.isEmpty(accIds)) {
      addWhere.accept("t.account_id IN (" + DaoUtils.placeholders(accIds.size()) + ")");
      params.addAll(accIds);
    }
    if (!CommonUtilities.isEmpty(expTypes)) {
      addWhere.accept("ti.exp_type IN (" + DaoUtils.placeholders(expTypes.size()) + ")");
      params.addAll(expTypes);
    }
    if (!CommonUtilities.isEmpty(catIds)) {
      addWhere.accept("ti.category_id IN (" + DaoUtils.placeholders(catIds.size()) + ")");
      params.addAll(catIds);
    }
    if (!CommonUtilities.isEmpty(catTypeIds)) {
      addWhere.accept("c.category_type_id IN (" + DaoUtils.placeholders(catTypeIds.size()) + ")");
      params.addAll(catTypeIds);
    }

    sql.append(" ORDER BY t.txn_date DESC");

    log.debug("[{}] Composite Transactions SQL=[{}]", requestId, sql);
    log.debug("[{}] Composite Transactions Params=[{}]", requestId, params);

    try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(stmt, params);
      }

      Map<UUID, TransactionResultBuilder> txnMap = new LinkedHashMap<>();

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          UUID txnId = rs.getObject("txn_id", UUID.class);
          TransactionResultBuilder txnBuilder = txnMap.get(txnId);
          if (txnBuilder == null) {
            AccountResponse.Account account =
                new AccountResponse.Account(
                    rs.getObject("account_id", UUID.class),
                    rs.getString("account_name"),
                    rs.getString("account_type"),
                    rs.getString("account_bank_name"),
                    rs.getDouble("account_opening_balance"),
                    rs.getString("account_status"));
            txnBuilder =
                new TransactionResultBuilder(
                    txnId,
                    rs.getObject("txn_date", LocalDateTime.class),
                    rs.getString("txn_merchant"),
                    account,
                    rs.getDouble("txn_total_amount"),
                    rs.getString("txn_notes"));
            txnMap.put(txnId, txnBuilder);
          }

          UUID itemId = rs.getObject("item_id", UUID.class);
          if (itemId != null) {
            CategoryTypeResponse.CategoryType ct =
                new CategoryTypeResponse.CategoryType(
                    rs.getObject("category_type_id", UUID.class),
                    rs.getString("category_type_name"));
            CategoryResponse.Category c =
                new CategoryResponse.Category(
                    rs.getObject("category_id", UUID.class), ct, rs.getString("category_name"));

            TransactionItemResponse.TransactionItem item =
                new TransactionItemResponse.TransactionItem(
                    itemId,
                    new TransactionResponse.Transaction(
                        txnId, null, null, 0.0, null, null, List.of()),
                    c,
                    rs.getString("item_label"),
                    rs.getDouble("item_amount"),
                    rs.getString("item_exp_type"));
            txnMap.get(txnId).addItem(item);
          }
        }
      }
      return txnMap.values().stream().map(TransactionResultBuilder::build).toList();
    }
  }

  public List<String> readAllMerchants() throws SQLException {
    String sql = "SELECT DISTINCT merchant FROM transaction ORDER BY merchant ASC";
    List<String> merchants = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        merchants.add(rs.getString("merchant"));
      }
    }
    return merchants;
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

  private static class TransactionResultBuilder {
    private final UUID id;
    private final LocalDateTime txnDate;
    private final String merchant;
    private final AccountResponse.Account account;
    private final double totalAmount;
    private final String notes;
    private final List<TransactionItemResponse.TransactionItem> items = new ArrayList<>();

    TransactionResultBuilder(
        UUID id,
        LocalDateTime txnDate,
        String merchant,
        AccountResponse.Account account,
        double totalAmount,
        String notes) {
      this.id = id;
      this.txnDate = txnDate;
      this.merchant = merchant;
      this.account = account;
      this.totalAmount = totalAmount;
      this.notes = notes;
    }

    void addItem(TransactionItemResponse.TransactionItem item) {
      items.add(item);
    }

    TransactionResponse.Transaction build() {
      return new TransactionResponse.Transaction(
          id, txnDate, merchant, totalAmount, notes, account, List.copyOf(items));
    }
  }
}
