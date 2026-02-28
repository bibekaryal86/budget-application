package budget.application.db.mapper;

import budget.application.db.util.DaoUtils;
import budget.application.model.dto.InsightsResponse;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class InsightsRowMappers {

  public static class CashFlowSummaryRowMapper
      implements RowMapper<InsightsResponse.CashFlowSummary> {
    @Override
    public InsightsResponse.CashFlowSummary map(ResultSet resultSet) throws SQLException {
      LocalDate beginDate = resultSet.getObject("begin_date", LocalDate.class);
      BigDecimal incomes = resultSet.getBigDecimal("incomes");
      BigDecimal expenses = resultSet.getBigDecimal("expenses");
      BigDecimal savings = resultSet.getBigDecimal("savings");
      BigDecimal balance = incomes.subtract(expenses).subtract(savings);
      return new InsightsResponse.CashFlowSummary(
          DaoUtils.getYearMonth(beginDate),
          new InsightsResponse.CashFlowAmounts(incomes, expenses, savings, balance));
    }
  }

  public static class CategoryAmountRowMapper
      implements RowMapper<InsightsResponse.CategoryAmount> {
    @Override
    public InsightsResponse.CategoryAmount map(ResultSet resultSet) throws SQLException {
      return new InsightsResponse.CategoryAmount(
          new CategoryRowMappers.CategoryRowMapperResponse().map(resultSet),
          resultSet.getBigDecimal("total_amount"));
    }
  }
}
