package budget.application.db.mapper;

import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.entity.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CategoryRowMappers {
  public static class CategoryRowMapper implements RowMapper<Category> {
    @Override
    public Category map(ResultSet rs) throws SQLException {
      return new Category(
          rs.getObject("id", UUID.class),
          rs.getObject("category_type_id", UUID.class),
          rs.getString("name"));
    }
  }

  public static class CategoryRowMapperResponse implements RowMapper<CategoryResponse.Category> {
    @Override
    public CategoryResponse.Category map(ResultSet rs) throws SQLException {
      return new CategoryResponse.Category(
          rs.getObject("category_id", UUID.class),
          new CategoryTypeResponse.CategoryType(
              rs.getObject("category_type_id", UUID.class), rs.getString("category_type_name")),
          rs.getString("category_name"));
    }
  }
}
