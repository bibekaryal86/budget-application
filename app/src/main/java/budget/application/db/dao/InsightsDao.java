package budget.application.db.dao;

import budget.application.db.mapper.InsightsRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.InsightsResponse;
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

public class InsightsDao {
  protected static final Logger log = LoggerFactory.getLogger(InsightsDao.class);

  private final Connection connection;
  private final InsightsRowMappers.CashFlowSummaryRowMapper cashFlowSummaryRowMapper;
  private final InsightsRowMappers.CategorySummaryRowMapper categorySummaryRowMapper;

  public InsightsDao(Connection connection) {
    this.connection = connection;
    this.cashFlowSummaryRowMapper = new InsightsRowMappers.CashFlowSummaryRowMapper();
    this.categorySummaryRowMapper = new InsightsRowMappers.CategorySummaryRowMapper();
  }

  public InsightsResponse.CashFlowSummary readCashFlowSummary(
      LocalDate beginDate, LocalDate endDate) throws SQLException {
    log.debug("Read cash flow summary: BeginDate=[{}], EndDate=[{}]", beginDate, endDate);

    List<InsightsResponse.CashFlowSummary> results = new ArrayList<>();
    String sql =
        """
              SELECT
                  ? AS begin_date,
                  ? AS end_date,
                  SUM(CASE WHEN ct.name = 'INCOME'  THEN ti.amount ELSE 0 END) AS incomes,
                  SUM(CASE WHEN ct.name = 'SAVINGS' THEN ti.amount ELSE 0 END) AS savings,
                  SUM(CASE WHEN ct.name NOT IN ('INCOME', 'SAVINGS') THEN ti.amount ELSE 0 END) AS expenses
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

    log.debug("Read Transaction Summary SQL=[{}]", sql);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, params, Boolean.TRUE);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(cashFlowSummaryRowMapper.map(rs));
        }
      }
    }
    return results.getFirst();
  }

  public List<InsightsResponse.CategorySummary> readCategorySummary(
      LocalDate beginDate, LocalDate endDate, List<UUID> categoryIds, List<UUID> categoryTypeIds)
      throws SQLException {
    log.debug(
        "Read category summary: BeginDate=[{}], EndDate=[{}], CategoryIds=[{}], CategoryTypeIds=[{}]",
        beginDate,
        endDate,
        categoryIds,
        categoryTypeIds);

    List<InsightsResponse.CategorySummary> results = new ArrayList<>();
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

    boolean hasCat = !CommonUtilities.isEmpty(categoryIds);
    boolean hasCatTypes = !CommonUtilities.isEmpty(categoryTypeIds);
    params.add(hasCat);
    params.add(categoryIds);
    params.add(hasCatTypes);
    params.add(categoryTypeIds);

    log.debug("Read Category Summary SQL=[{}]", sql);

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(categorySummaryRowMapper.map(resultSet));
        }
      }
    }
    return results;
  }
}
