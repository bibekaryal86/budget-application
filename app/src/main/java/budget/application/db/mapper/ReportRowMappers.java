package budget.application.db.mapper;

import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.dto.ReportResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class ReportRowMappers {

  public static class TransactionSummaryRowMapper
      implements RowMapper<ReportResponse.TransactionSummary> {
    @Override
    public ReportResponse.TransactionSummary map(ResultSet rs) throws SQLException {
      return new ReportResponse.TransactionSummary(
          rs.getObject("begin_date", LocalDate.class),
          rs.getObject("end_date", LocalDate.class),
          rs.getBigDecimal("INCOMES"),
          rs.getBigDecimal("EXPENSES"),
          rs.getBigDecimal("SAVINGS"));
    }
  }

  public static class CategorySummaryRowMapper
      implements RowMapper<ReportResponse.CategorySummary> {
    @Override
    public ReportResponse.CategorySummary map(ResultSet rs) throws SQLException {
      return new ReportResponse.CategorySummary(
          new CategoryResponse.Category(
              rs.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name")),
              rs.getString("category_name")),
          rs.getBigDecimal("total_amount"));
    }
  }
}
