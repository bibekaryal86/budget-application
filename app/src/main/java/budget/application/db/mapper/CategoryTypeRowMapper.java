package budget.application.db.mapper;

import budget.application.model.entities.CategoryType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class CategoryTypeRowMapper implements RowMapper<CategoryType> {
  @Override
  public CategoryType map(ResultSet rs) throws SQLException {
    return CategoryType.builder()
        .id(rs.getObject("id", UUID.class))
        .name(rs.getString("name"))
        .createdAt(rs.getObject("created_at", LocalDateTime.class))
        .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
        .build();
  }
}
