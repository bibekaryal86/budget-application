package budget.application.db.mapper;

import budget.application.model.entity.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CategoryRowMapper implements RowMapper<Category> {
  @Override
  public Category map(ResultSet rs) throws SQLException {
    return new Category(
        rs.getObject("id", UUID.class),
        rs.getObject("category_type_id", UUID.class),
        rs.getString("name"));
  }
}
