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
    public InsightsResponse.CashFlowSummary map(ResultSet resultSet) throws SQLException {
      return new InsightsResponse.CashFlowSummary(
          resultSet.getObject("begin_date", LocalDate.class),
          resultSet.getObject("end_date", LocalDate.class),
          resultSet.getBigDecimal("incomes"),
          resultSet.getBigDecimal("expenses"),
          resultSet.getBigDecimal("savings"));
    }
  }

  public static class CategorySummaryRowMapper
      implements RowMapper<InsightsResponse.CategorySummary> {
    @Override
    public InsightsResponse.CategorySummary map(ResultSet resultSet) throws SQLException {
      return new InsightsResponse.CategorySummary(
          resultSet.getObject("begin_date", LocalDate.class),
          resultSet.getObject("end_date", LocalDate.class),
          new CategoryResponse.Category(
              resultSet.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  resultSet.getObject("category_type_id", UUID.class),
                  resultSet.getString("category_type_name")),
              resultSet.getString("category_name")),
          resultSet.getBigDecimal("total_amount"));
    }
  }
}
