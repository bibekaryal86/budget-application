package budget.application.db.dao;

import budget.application.db.mapper.ReportRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.ReportResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportDao {
  protected static final Logger log = LoggerFactory.getLogger(ReportDao.class);

  private final String requestId;
  private final Connection connection;
  private final ReportRowMappers.TransactionSummaryRowMapper txnSummaryMapper;

  public ReportDao(String requestId, Connection connection) {
    this.requestId = requestId;
    this.connection = connection;
    this.txnSummaryMapper = new ReportRowMappers.TransactionSummaryRowMapper();
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
}
