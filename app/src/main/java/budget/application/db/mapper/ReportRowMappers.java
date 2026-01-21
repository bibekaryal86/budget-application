package budget.application.db.mapper;

import budget.application.model.dto.ReportResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportRowMappers {

  public static class TransactionSummaryRowMapper
      implements RowMapper<ReportResponse.TransactionSummary> {
    @Override
    public ReportResponse.TransactionSummary map(ResultSet rs) throws SQLException {
      return new ReportResponse.TransactionSummary(
          rs.getObject("begin_date", LocalDate.class).atStartOfDay(),
          rs.getObject("end_date", LocalDate.class).atStartOfDay(),
          rs.getBigDecimal("INCOMES"),
          rs.getBigDecimal("EXPENSES"),
          rs.getBigDecimal("SAVINGS"));
    }
  }
}
