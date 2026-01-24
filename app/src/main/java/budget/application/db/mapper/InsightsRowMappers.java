package budget.application.db.mapper;

import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.dto.InsightsResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class InsightsRowMappers {

  public static class CashFlowSummaryRowMapper
      implements RowMapper<InsightsResponse.CashFlowSummary> {
    @Override
    public InsightsResponse.CashFlowSummary map(ResultSet rs) throws SQLException {
      return new InsightsResponse.CashFlowSummary(
          rs.getObject("begin_date", LocalDate.class),
          rs.getObject("end_date", LocalDate.class),
          rs.getBigDecimal("incomes"),
          rs.getBigDecimal("expenses"),
          rs.getBigDecimal("savings"));
    }
  }

  public static class CategorySummaryRowMapper
      implements RowMapper<InsightsResponse.CategorySummary> {
    @Override
    public InsightsResponse.CategorySummary map(ResultSet rs) throws SQLException {
      return new InsightsResponse.CategorySummary(
          rs.getObject("begin_date", LocalDate.class),
          rs.getObject("end_date", LocalDate.class),
          new CategoryResponse.Category(
              rs.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name")),
              rs.getString("category_name")),
          rs.getBigDecimal("total_amount"));
    }
  }
}
