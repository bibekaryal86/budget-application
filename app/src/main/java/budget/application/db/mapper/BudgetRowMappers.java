package budget.application.db.mapper;

import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.entity.Budget;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetRowMappers {
  public static class BudgetRowMapper implements RowMapper<Budget> {
    @Override
    public Budget map(ResultSet resultSet) throws SQLException {
      return new Budget(
          resultSet.getObject("id", UUID.class),
          resultSet.getObject("category_id", UUID.class),
          resultSet.getInt("budget_month"),
          resultSet.getInt("budget_year"),
          resultSet.getBigDecimal("amount"),
          resultSet.getString("notes"),
          resultSet.getObject("created_at", LocalDateTime.class),
          resultSet.getObject("updated_at", LocalDateTime.class));
    }
  }

  public static class BudgetRowMapperResponse implements RowMapper<BudgetResponse.Budget> {
    @Override
    public BudgetResponse.Budget map(ResultSet resultSet) throws SQLException {
      return new BudgetResponse.Budget(
          resultSet.getObject("budget_id", UUID.class),
          new CategoryResponse.Category(
              resultSet.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  resultSet.getObject("category_type_id", UUID.class),
                  resultSet.getString("category_type_name")),
              resultSet.getString("category_name")),
          resultSet.getInt("budget_month"),
          resultSet.getInt("budget_year"),
          resultSet.getBigDecimal("budget_amount"),
          resultSet.getString("budget_notes"));
    }
  }
}
