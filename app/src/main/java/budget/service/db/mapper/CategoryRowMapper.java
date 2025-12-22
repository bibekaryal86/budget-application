package budget.service.db.mapper;

import budget.service.model.entities.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class CategoryRowMapper implements RowMapper<Category> {
  @Override
  public Category map(ResultSet rs) throws SQLException {
    return Category.builder()
        .id(rs.getObject("id", UUID.class))
        .categoryTypeId(rs.getObject("category_type_id", UUID.class))
        .name(rs.getString("name"))
        .createdAt(rs.getObject("created_at", LocalDateTime.class))
        .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
        .build();
  }
}
