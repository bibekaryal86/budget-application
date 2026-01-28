package budget.application.db.mapper;

import budget.application.model.entity.CategoryType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CategoryTypeRowMapper implements RowMapper<CategoryType> {
  @Override
  public CategoryType map(ResultSet resultSet) throws SQLException {
    return new CategoryType(resultSet.getObject("id", UUID.class), resultSet.getString("name"));
  }
}
