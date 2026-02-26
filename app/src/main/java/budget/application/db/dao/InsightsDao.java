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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightsDao {
  protected static final Logger log = LoggerFactory.getLogger(InsightsDao.class);

  private final Connection connection;
  private final InsightsRowMappers.CashFlowSummaryRowMapper cashFlowSummaryRowMapper;
  private final InsightsRowMappers.CategoryAmountRowMapper categoryAmountRowMapper;

  public InsightsDao(Connection connection) {
    this.connection = connection;
    this.cashFlowSummaryRowMapper = new InsightsRowMappers.CashFlowSummaryRowMapper();
    this.categoryAmountRowMapper = new InsightsRowMappers.CategoryAmountRowMapper();
  }

  public List<InsightsResponse.CashFlowSummary> readCashFlowSummary(
      LocalDate beginDate, LocalDate endDate, int totalMonths) throws SQLException {
    log.debug(
        "Read cash flow summary: BeginDate=[{}], EndDate=[{}], TotalMonths=[{}]",
        beginDate,
        endDate,
        totalMonths);

    String sql =
        """
              WITH RECURSIVE date_intervals AS (
                -- Start with the end date's month as the first interval
                SELECT
                  DATE_TRUNC('month', ?::date)::date AS interval_start,
                  (DATE_TRUNC('month', ?::date) + INTERVAL '1 month' - INTERVAL '1 day')::date AS interval_end,
                  1 AS month_offset
                UNION ALL
                SELECT
                  (DATE_TRUNC('month', interval_start) - INTERVAL '1 month')::date,
                  (DATE_TRUNC('month', interval_start) - INTERVAL '1 day')::date,
                  month_offset + 1
                FROM date_intervals
                WHERE month_offset < ?
              )
              SELECT
                  di.interval_start AS begin_date,
                  di.interval_end AS end_date,
                  COALESCE(SUM(CASE WHEN ct.name = 'INCOME' THEN ti.amount ELSE 0 END), 0) AS incomes,
                  COALESCE(SUM(CASE WHEN ct.name = 'SAVINGS' THEN ti.amount ELSE 0 END), 0) AS savings,
                  COALESCE(SUM(CASE WHEN ct.name NOT IN ('INCOME', 'SAVINGS', 'TRANSFER') THEN ti.amount ELSE 0 END), 0) AS expenses
              FROM date_intervals di
              LEFT JOIN transaction t
                  ON t.txn_date >= di.interval_start
                  AND t.txn_date <= di.interval_end
              LEFT JOIN transaction_item ti
                  ON ti.transaction_id = t.id
              LEFT JOIN category c
                  ON ti.category_id = c.id
              LEFT JOIN category_type ct
                  ON c.category_type_id = ct.id
              GROUP BY di.interval_start, di.interval_end
              ORDER BY di.interval_start DESC;
          """;

    List<Object> params = new ArrayList<>();
    params.add(endDate);
    params.add(endDate);
    params.add(totalMonths);

    log.debug("Read Transaction Summary SQL=[{}]", sql);
    List<InsightsResponse.CashFlowSummary> results = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(cashFlowSummaryRowMapper.map(resultSet));
        }
      }
    }

    return results;
  }

  public List<InsightsResponse.CategorySummary> readCategorySummary(
      LocalDate beginDate,
      LocalDate endDate,
      List<UUID> categoryIds,
      List<UUID> categoryTypeIds,
      int totalMonths)
      throws SQLException {

    log.debug(
        "Read category summary: BeginDate=[{}], EndDate=[{}], CategoryIds=[{}], CategoryTypeIds=[{}], TotalMonths=[{}]",
        beginDate,
        endDate,
        categoryIds,
        categoryTypeIds,
        totalMonths);

    String sql =
        """
        WITH RECURSIVE date_intervals AS (
            -- Start with the end date's month as the first interval
            SELECT
              DATE_TRUNC('month', ?::date)::date AS interval_start,
              (DATE_TRUNC('month', ?::date) + INTERVAL '1 month' - INTERVAL '1 day')::date AS interval_end,
              1 AS month_offset
            UNION ALL
            SELECT
              (DATE_TRUNC('month', interval_start) - INTERVAL '1 month')::date,
              (DATE_TRUNC('month', interval_start) - INTERVAL '1 day')::date,
              month_offset + 1
            FROM date_intervals
            WHERE month_offset < ?
          )
        SELECT
            di.interval_start AS begin_date,
            di.interval_end AS end_date,
            c.id AS category_id,
            c.name AS category_name,
            ct.id AS category_type_id,
            ct.name AS category_type_name,
            COALESCE(SUM(ti.amount), 0) AS total_amount
        FROM date_intervals di
        CROSS JOIN category c
        JOIN category_type ct ON c.category_type_id = ct.id
        LEFT JOIN transaction t
            ON t.txn_date >= di.interval_start
            AND t.txn_date <= di.interval_end
        LEFT JOIN transaction_item ti
            ON ti.transaction_id = t.id
            AND ti.category_id = c.id
            WHERE ( ? = FALSE OR c.id = ANY(?) )
            AND ( ? = FALSE OR ct.id = ANY(?) )
        GROUP BY di.interval_start, di.interval_end, c.id, c.name, ct.id, ct.name
        ORDER BY di.interval_start DESC, c.name
        """;

    List<Object> params = new ArrayList<>();
    params.add(endDate);
    params.add(endDate);
    params.add(totalMonths);

    boolean hasCategoryIds = !CommonUtilities.isEmpty(categoryIds);
    boolean hasCategoryTypeIds = !CommonUtilities.isEmpty(categoryTypeIds);

    params.add(hasCategoryIds);
    params.add(hasCategoryIds ? categoryIds.toArray(new UUID[0]) : null);
    params.add(hasCategoryTypeIds);
    params.add(hasCategoryTypeIds ? categoryTypeIds.toArray(new UUID[0]) : null);

    log.debug("Read Category Summary SQL=[{}]", sql);

    Map<String, InsightsResponse.CategorySummary> categorySummaryMap = new LinkedHashMap<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          LocalDate beginDateRes = resultSet.getObject("begin_date", LocalDate.class);
          String yearMonth = DaoUtils.getYearMonth(beginDateRes);

          InsightsResponse.CategoryAmount categoryAmount = categoryAmountRowMapper.map(resultSet);

          categorySummaryMap
              .computeIfAbsent(
                  yearMonth,
                  k -> new InsightsResponse.CategorySummary(yearMonth, new ArrayList<>()))
              .categoryAmounts()
              .add(categoryAmount);
        }
      }
    }
    return new ArrayList<>(categorySummaryMap.values());
  }
}
