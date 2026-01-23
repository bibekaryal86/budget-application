package budget.application.db.dao;

import budget.application.db.mapper.ReportRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.ReportResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportDao {
  protected static final Logger log = LoggerFactory.getLogger(ReportDao.class);

  private final String requestId;
  private final Connection connection;
  private final ReportRowMappers.TransactionSummaryRowMapper txnSummaryMapper;
  private final ReportRowMappers.CategorySummaryRowMapper catSummaryMapper;

  public ReportDao(String requestId, Connection connection) {
    this.requestId = requestId;
    this.connection = connection;
    this.txnSummaryMapper = new ReportRowMappers.TransactionSummaryRowMapper();
    this.catSummaryMapper = new ReportRowMappers.CategorySummaryRowMapper();
  }

  public ReportResponse.TransactionSummary readTransactionSummary(
      LocalDate beginDate, LocalDate endDate) throws SQLException {
    log.debug(
        "[{}] Read transaction summary: BeginDate=[{}], EndDate=[{}]",
        requestId,
        beginDate,
        endDate);

    List<ReportResponse.TransactionSummary> results = new ArrayList<>();
    String sql =
        """
              SELECT
                  ? AS begin_date,
                  ? AS end_date,
                  SUM(CASE WHEN ct.name = 'INCOME'  THEN ti.amount ELSE 0 END) AS INCOMES,
                  SUM(CASE WHEN ct.name = 'SAVINGS' THEN ti.amount ELSE 0 END) AS SAVINGS,
                  SUM(CASE WHEN ct.name NOT IN ('INCOME', 'SAVINGS') THEN ti.amount ELSE 0 END) AS EXPENSES
              FROM transaction_item ti
              JOIN transaction t
                  ON ti.transaction_id = t.id
              JOIN category c
                  ON ti.category_id = c.id
              JOIN category_type ct
                  ON c.category_type_id = ct.id
              WHERE t.txn_date >= ?
                AND t.txn_date <= ?;
          """;

    List<Object> params = new ArrayList<>();
    params.add(beginDate);
    params.add(endDate);
    // used twice, so need to provide twice
    params.add(beginDate);
    params.add(endDate);

    log.debug("[{}] Read Transaction Summary SQL=[{}]", requestId, sql);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(txnSummaryMapper.map(rs));
        }
      }
    }
    return results.getFirst();
  }

  public List<ReportResponse.CategorySummary> readCategorySummary(
      LocalDate beginDate, LocalDate endDate, List<UUID> catIds, List<UUID> catTypeIds)
      throws SQLException {
    log.debug(
        "[{}] Read category summary: BeginDate=[{}], EndDate=[{}], CategoryIds=[{}], CategoryTypeIds=[{}]",
        requestId,
        beginDate,
        endDate,
        catIds,
        catTypeIds);

    List<ReportResponse.CategorySummary> results = new ArrayList<>();
    String sql =
        """
                  SELECT
                      ? AS begin_date,
                      ? AS end_date,
                      c.id AS category_id,
                      c.name AS category_name,
                      ct.id AS category_type_id,
                      ct.name AS category_type_name,
                      SUM(ti.amount) AS total_amount
                  FROM transaction_item ti
                  JOIN transaction t ON ti.transaction_id = t.id
                  JOIN category c ON ti.category_id = c.id
                  JOIN category_type ct ON c.category_type_id = ct.id
                  WHERE t.txn_date >= ?
                    AND t.txn_date <= ?
                    AND ( ? = FALSE OR c.id = ANY(?) )
                    AND ( ? = FALSE OR ct.id = ANY(?) )
                  GROUP BY c.id, c.name, ct.id, ct.name
                  ORDER BY c.name;
              """;

    List<Object> params = new ArrayList<>();
    params.add(beginDate);
    params.add(endDate);
    // used twice, so need to provide twice
    params.add(beginDate);
    params.add(endDate);

    boolean hasCat = !CommonUtilities.isEmpty(catIds);
    boolean hasCatTypes = !CommonUtilities.isEmpty(catTypeIds);
    params.add(hasCat);
    params.add(catIds);
    params.add(hasCatTypes);
    params.add(catTypeIds);

    log.debug("[{}] Read Category Summary SQL=[{}]", requestId, sql);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(catSummaryMapper.map(rs));
        }
      }
    }
    return results;
  }
}
