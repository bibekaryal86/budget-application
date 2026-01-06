package budget.application.db.mapper;

import budget.application.model.entity.CategoryType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CategoryTypeRowMapper implements RowMapper<CategoryType> {
  @Override
  public CategoryType map(ResultSet rs) throws SQLException {
    return new CategoryType(rs.getObject("id", UUID.class), rs.getString("name"));
  }
}
