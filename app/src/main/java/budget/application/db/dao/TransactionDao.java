package budget.application.db.dao;

import budget.application.common.Constants;
import budget.application.db.mapper.AccountRowMappers;
import budget.application.db.mapper.CategoryRowMappers;
import budget.application.db.mapper.TransactionRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.PaginationResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.model.dto.TransactionResponse;
import budget.application.model.entity.Transaction;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.sql.Array;
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

  public TransactionDao(Connection connection) {
    super(connection, new TransactionRowMappers.TransactionRowMapper(), null);
  }

  @Override
  protected String tableName() {
    return "transaction";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("txn_date", "merchant", "total_amount");
  }

  @Override
  protected List<Object> insertValues(Transaction transaction) {
    return List.of(
        transaction.txnDate().toLocalDate(),
        transaction.merchant().toUpperCase(),
        transaction.totalAmount());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("txn_date", "merchant", "total_amount", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Transaction transaction) {
    return List.of(
        transaction.txnDate().toLocalDate(),
        transaction.merchant().toUpperCase(),
        transaction.totalAmount(),
        LocalDateTime.now());
  }

  @Override
  protected UUID getId(Transaction transaction) {
    return transaction.id();
  }

  @Override
  protected String orderByClause() {
    return "txn_date DESC";
  }

  public PaginationResponse<TransactionResponse.Transaction> readTransactions(
      List<UUID> transactionIds,
      RequestParams.TransactionParams requestParams,
      PaginationRequest paginationRequest)
      throws SQLException {
    log.debug("Read Transactions: TransactionIds=[{}], Params=[{}]", transactionIds, requestParams);

    if (requestParams == null) {
      requestParams =
          new RequestParams.TransactionParams(
              null, null, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    if (paginationRequest == null) {
      paginationRequest =
          new PaginationRequest(Constants.DEFAULT_PAGE_NUMBER, Constants.DEFAULT_PER_PAGE);
    }

    List<UUID> categoryIds = requestParams.categoryIds();
    List<UUID> categoryTypeIds = requestParams.categoryTypeIds();
    LocalDate beginDate = requestParams.beginDate();
    LocalDate endDate = requestParams.endDate();
    List<String> merchants = requestParams.merchants();
    List<UUID> accountIds = requestParams.accountIds();
    List<String> tags = requestParams.tags();
    int pageNumber = paginationRequest.pageNumber();
    int perPage = paginationRequest.perPage();

    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    t.id           AS txn_id,
                    t.txn_date     AS txn_date,
                    t.merchant     AS txn_merchant,
                    t.total_amount AS txn_total_amount,
                    ti.id          AS item_id,
                    ti.amount      AS item_amount,
                    ti.tags        AS item_tags,
                    ti.notes       AS item_notes,
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
                LEFT JOIN transaction_item ti
                       ON ti.transaction_id = t.id
                LEFT JOIN category c
                       ON c.id = ti.category_id
                LEFT JOIN category_type ct
                       ON ct.id = c.category_type_id
                LEFT JOIN account a
                    ON ti.account_id = a.id
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
    if (!CommonUtilities.isEmpty(transactionIds)) {
      addWhere.accept("t.id IN (" + DaoUtils.placeholders(transactionIds.size()) + ")");
      params.addAll(transactionIds);
    }
    if (!CommonUtilities.isEmpty(merchants)) {
      addWhere.accept("t.merchant IN (" + DaoUtils.placeholders(merchants.size()) + ")");
      params.addAll(merchants);
    }
    if (!CommonUtilities.isEmpty(accountIds)) {
      addWhere.accept("ti.account_id IN (" + DaoUtils.placeholders(accountIds.size()) + ")");
      params.addAll(accountIds);
    }
    if (!CommonUtilities.isEmpty(categoryIds)) {
      addWhere.accept("ti.category_id IN (" + DaoUtils.placeholders(categoryIds.size()) + ")");
      params.addAll(categoryIds);
    }
    if (!CommonUtilities.isEmpty(categoryTypeIds)) {
      addWhere.accept(
          "c.category_type_id IN (" + DaoUtils.placeholders(categoryTypeIds.size()) + ")");
      params.addAll(categoryTypeIds);
    }
    if (!CommonUtilities.isEmpty(tags)) {
      Array sqlArray = connection.createArrayOf("text", tags.toArray());
      addWhere.accept("ti.tags && ?");
      params.add(sqlArray);
    }

    sql.append(" ORDER BY t.txn_date DESC");
    sql.append(" LIMIT ? OFFSET ?");
    int limit = perPage;
    int offset = (pageNumber - 1) * perPage;

    log.debug("Read Transactions SQL=[{}]", sql);

    List<TransactionResponse.Transaction> transactions;

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      }
      preparedStatement.setInt(params.size() + 1, limit);
      preparedStatement.setInt(params.size() + 2, offset);

      Map<UUID, TransactionResultBuilder> transactionResultBuilderMap = new LinkedHashMap<>();

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          UUID transactionId = resultSet.getObject("txn_id", UUID.class);
          TransactionResultBuilder transactionResultBuilder =
              transactionResultBuilderMap.get(transactionId);
          if (transactionResultBuilder == null) {
            transactionResultBuilder =
                new TransactionResultBuilder(
                    transactionId,
                    resultSet.getObject("txn_date", LocalDateTime.class),
                    resultSet.getString("txn_merchant"),
                    resultSet.getBigDecimal("txn_total_amount"));
            transactionResultBuilderMap.put(transactionId, transactionResultBuilder);
          }

          UUID transactionItemId = resultSet.getObject("item_id", UUID.class);
          if (transactionItemId != null) {
            AccountResponse.Account account =
                new AccountRowMappers.AccountRowMapperResponse().map(resultSet);
            CategoryResponse.Category category =
                new CategoryRowMappers.CategoryRowMapperResponse().map(resultSet);
            TransactionItemResponse.TransactionItem item =
                new TransactionItemResponse.TransactionItem(
                    transactionItemId,
                    new TransactionResponse.Transaction(transactionId, null, null, null, List.of()),
                    category,
                    account,
                    resultSet.getBigDecimal("item_amount"),
                    List.of((String[]) resultSet.getArray("item_tags").getArray()),
                    resultSet.getString("item_notes"));
            transactionResultBuilderMap.get(transactionId).addItem(item);
          }
        }
      }
      transactions =
          transactionResultBuilderMap.values().stream()
              .map(TransactionResultBuilder::build)
              .toList();
    }

    int totalItems = countAll();
    int totalPages = (int) Math.ceil((double) totalItems / perPage);
    ResponseMetadata.ResponsePageInfo pageInfo =
        new ResponseMetadata.ResponsePageInfo(totalItems, totalPages, pageNumber, perPage);

    return new PaginationResponse<>(transactions, pageInfo);
  }

  public List<String> readAllMerchants() throws SQLException {
    String sql = "SELECT DISTINCT merchant FROM transaction ORDER BY merchant ASC";
    List<String> merchants = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        merchants.add(resultSet.getString("merchant"));
      }
    }
    return merchants;
  }

  private int countAll() throws SQLException {
    String sql = "SELECT COUNT(*) FROM transaction";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {

      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static class TransactionResultBuilder {
    private final UUID id;
    private final LocalDateTime transactionDate;
    private final String merchant;
    private final BigDecimal totalAmount;
    private final List<TransactionItemResponse.TransactionItem> transactionItems =
        new ArrayList<>();

    TransactionResultBuilder(
        UUID id, LocalDateTime transactionDate, String merchant, BigDecimal totalAmount) {
      this.id = id;
      this.transactionDate = transactionDate;
      this.merchant = merchant;
      this.totalAmount = totalAmount;
    }

    void addItem(TransactionItemResponse.TransactionItem item) {
      transactionItems.add(item);
    }

    TransactionResponse.Transaction build() {
      return new TransactionResponse.Transaction(
          id, transactionDate, merchant, totalAmount, List.copyOf(transactionItems));
    }
  }
}
