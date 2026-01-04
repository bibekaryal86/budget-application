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
    public Budget map(ResultSet rs) throws SQLException {
      return new Budget(
          rs.getObject("id", UUID.class),
          rs.getObject("category_id", UUID.class),
          rs.getInt("budget_month"),
          rs.getInt("budget_year"),
          rs.getBigDecimal("amount"),
          rs.getString("notes"),
          rs.getObject("created_at", LocalDateTime.class),
          rs.getObject("updated_at", LocalDateTime.class));
    }
  }

  public static class BudgetRowMapperResponse implements RowMapper<BudgetResponse.Budget> {
    @Override
    public BudgetResponse.Budget map(ResultSet rs) throws SQLException {
      return new BudgetResponse.Budget(
          rs.getObject("id", UUID.class),
          new CategoryResponse.Category(
              rs.getObject("category_id", UUID.class),
              new CategoryTypeResponse.CategoryType(
                  rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name")),
              rs.getString("category_name")),
          rs.getInt("budget_month"),
          rs.getInt("budget_year"),
          rs.getBigDecimal("amount"),
          rs.getString("notes"));
    }
  }
}
