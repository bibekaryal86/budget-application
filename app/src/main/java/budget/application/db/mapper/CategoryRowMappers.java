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
    public Category map(ResultSet resultSet) throws SQLException {
      return new Category(
          resultSet.getObject("id", UUID.class),
          resultSet.getObject("category_type_id", UUID.class),
          resultSet.getString("name"));
    }
  }

  public static class CategoryRowMapperResponse implements RowMapper<CategoryResponse.Category> {
    @Override
    public CategoryResponse.Category map(ResultSet resultSet) throws SQLException {
      return new CategoryResponse.Category(
          resultSet.getObject("category_id", UUID.class),
          new CategoryTypeResponse.CategoryType(
              resultSet.getObject("category_type_id", UUID.class),
              resultSet.getString("category_type_name")),
          resultSet.getString("category_name"));
    }
  }
}
