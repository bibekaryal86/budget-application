package budget.application.db.dao;

import budget.application.db.mapper.CategoryRowMappers;
import budget.application.db.mapper.RowMapper;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.entity.Category;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDao extends BaseDao<Category> {

  private final RowMapper<CategoryResponse.Category> catRespMapper;

  public CategoryDao(String requestId, Connection connection) {
    super(requestId, connection, new CategoryRowMappers.CategoryRowMapper());
    this.catRespMapper = new CategoryRowMappers.CategoryRowMapperResponse();
  }

  @Override
  protected String tableName() {
    return "category";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("category_type_id", "name");
  }

  @Override
  protected List<Object> insertValues(Category c) {
    return List.of(c.categoryTypeId(), c.name().toUpperCase());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_type_id", "name");
  }

  @Override
  protected List<Object> updateValues(Category c) {
    return List.of(c.categoryTypeId(), c.name().toUpperCase());
  }

  @Override
  protected UUID getId(Category c) {
    return c.id();
  }

  @Override
  protected String orderByClause() {
    return "name ASC";
  }

  public Optional<CategoryResponse.Category> readByIdNoEx(UUID uuid) {
    try {
      return readCategories(List.of(uuid), List.of()).stream().findFirst();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<CategoryResponse.Category> readCategories(List<UUID> catIds, List<UUID> catTypeIds)
      throws SQLException {
    log.debug("[{}] Read Categories: CatIds=[{}], CatTypeIds=[{}]", requestId, catIds, catTypeIds);
    StringBuilder sql =
        new StringBuilder(
            """
          SELECT
              c.id AS category_id,
              c.name AS category_name,
              ct.id AS category_type_id,
              ct.name AS category_type_name
          FROM category c
          JOIN category_type ct
            ON ct.id = c.category_type_id
          """);

    boolean first = true;
    if (!CommonUtilities.isEmpty(catIds)) {
      sql.append(" WHERE c.id IN (").append(DaoUtils.placeholders(catIds.size())).append(")");
      first = false;
    }
    if (!CommonUtilities.isEmpty(catTypeIds)) {
      sql.append(first ? " AND " : " WHERE ");
      sql.append(" ct.id IN (").append(DaoUtils.placeholders(catTypeIds.size())).append(")");
    }
    sql.append(" ORDER BY ct.name, c.name ASC ");

    log.debug("[{}] Read Categories SQL=[{}]", requestId, sql);

    try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
      if (!CommonUtilities.isEmpty(catIds)) {
        DaoUtils.bindParams(ps, catIds);
      }
      if (!CommonUtilities.isEmpty(catTypeIds)) {
        DaoUtils.bindParams(ps, catTypeIds);
      }

      List<CategoryResponse.Category> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(catRespMapper.map(rs));
        }
      }
      return results;
    }
  }
}
