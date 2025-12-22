package budget.application.db.dao;

import budget.application.db.mapper.RowMapper;
import budget.application.db.util.DaoUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseDao<T> {

  protected final Connection connection;
  protected final RowMapper<T> mapper;

  protected BaseDao(final Connection connection, final RowMapper<T> mapper) {
    this.connection = connection;
    this.mapper = mapper;
  }

  // ---- ABSTRACT CONTRACT ----

  protected abstract String tableName();

  protected abstract List<String> insertColumns();

  protected abstract List<Object> insertValues(T entity);

  protected abstract List<String> updateColumns();

  protected abstract List<Object> updateValues(T entity);

  protected abstract UUID getId(T entity);

  // ---- GENERIC CRUD ----

  // 1) CREATE
  public T create(T entity) throws SQLException {
    String cols = String.join(", ", insertColumns());
    String placeholders = DaoUtils.placeholders(insertColumns().size());

    String sql =
        "INSERT INTO " + tableName() + " (" + cols + ") VALUES (" + placeholders + ") RETURNING *";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, insertValues(entity));
      stmt.executeUpdate();
    }

    return entity;
  }

  // 2) READ (readAll or readByIds)
  public List<T> read(List<UUID> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
      return readAll();
    } else {
      return readByIds(ids);
    }
  }

  private List<T> readAll() throws SQLException {
    String sql = "SELECT * FROM " + tableName();

    try (PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<T> results = new ArrayList<>();
      while (rs.next()) results.add(mapper.map(rs));
      return results;
    }
  }

  private List<T> readByIds(List<UUID> ids) throws SQLException {
    String sql =
        "SELECT * FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, ids);

      try (ResultSet rs = stmt.executeQuery()) {
        List<T> results = new ArrayList<>();
        while (rs.next()) results.add(mapper.map(rs));
        return results;
      }
    }
  }

  // 3) UPDATE
  public T update(T entity) throws SQLException {
    List<String> cols = updateColumns();
    List<Object> values = updateValues(entity);

    String setClause = String.join(", ", cols.stream().map(c -> c + " = ?").toList());

    String sql = "UPDATE " + tableName() + " SET " + setClause + " WHERE id = ? RETURNING *";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, values);
      stmt.setObject(values.size() + 1, getId(entity));
      stmt.executeUpdate();
    }

    return entity;
  }

  // 4) DELETE
  public int delete(List<UUID> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) return 0;

    String sql =
        "DELETE FROM " + tableName() + " WHERE id IN (" + DaoUtils.placeholders(ids.size()) + ")";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      DaoUtils.bindParams(stmt, ids);
      return stmt.executeUpdate();
    }
  }
}
