package budget.application.db.dao;

import budget.application.db.mapper.BudgetRowMappers;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.entity.Budget;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BudgetDao extends BaseDao<Budget> {

  private final BudgetRowMappers.BudgetRowMapperResponse budRespRowMapper;

  public BudgetDao(String requestId, Connection connection) {
    super(requestId, connection, new BudgetRowMappers.BudgetRowMapper());
    this.budRespRowMapper = new BudgetRowMappers.BudgetRowMapperResponse();
  }

  @Override
  protected String tableName() {
    return "budget";
  }

  @Override
  protected List<String> insertColumns() {
    return List.of("category_id", "budget_month", "budget_year", "amount", "notes");
  }

  @Override
  protected List<Object> insertValues(Budget budget) {
    return List.of(
        budget.categoryId(),
        budget.budgetMonth(),
        budget.budgetYear(),
        budget.amount(),
        budget.notes());
  }

  @Override
  protected List<String> updateColumns() {
    return List.of("category_id", "budget_month", "budget_year", "amount", "notes", "updated_at");
  }

  @Override
  protected List<Object> updateValues(Budget budget) {
    return List.of(
        budget.categoryId(),
        budget.budgetMonth(),
        budget.budgetYear(),
        budget.amount(),
        budget.notes(),
        LocalDateTime.now());
  }

  @Override
  protected UUID getId(Budget acc) {
    return acc.id();
  }

  @Override
  protected String orderByClause() {
    return "budget_year, budget_month DESC";
  }

  public List<BudgetResponse.Budget> readBudgets(
      List<UUID> ids, int budgetMonth, int budgetYear, List<UUID> catIds) throws SQLException {
    log.debug(
        "[{}] Read Budgets: Ids={}, Month=[{}], Year=[{}], CatIds={}",
        requestId,
        ids,
        budgetMonth,
        budgetYear,
        catIds);
    StringBuilder sql =
        new StringBuilder(
            """
                        SELECT
                            b.id           AS budget_id,
                            b.budget_month     AS budget_month,
                            b.budget_year     AS budget_year,
                            t.amount AS budget_amount,
                            t.notes        AS budget_notes,
                            c.id           AS category_id,
                            c.name         AS category_name,
                            ct.id          AS category_type_id,
                            ct.name        AS category_type_name
                        FROM budget b
                        JOIN category c
                             ON c.id = b.category_id
                        JOIN category_type ct
                             ON ct.id = c.category_type_id
                        """);

    List<Object> params = new ArrayList<>();
    final boolean[] whereAdded = {false};

    Consumer<String> addWhere =
        (condition) -> {
          sql.append(whereAdded[0] ? " AND " : " WHERE ");
          sql.append(condition);
          whereAdded[0] = true;
        };

    if (!CommonUtilities.isEmpty(ids)) {
      addWhere.accept("b.id IN (" + DaoUtils.placeholders(ids.size()) + ")");
      params.addAll(ids);
    }
    if (budgetMonth > 0 && budgetYear > 0) {
      addWhere.accept("b.budget_month = ? AND b.budget_year = ?");
      params.add(budgetMonth);
      params.add(budgetYear);
    }
    if (!CommonUtilities.isEmpty(catIds)) {
      addWhere.accept("b.category_id IN (" + DaoUtils.placeholders(catIds.size()) + ")");
      params.addAll(catIds);
    }
    sql.append(" ct.name, c.name ASC");

    log.debug("[{}] Read Budgets SQL=[{}]", requestId, sql);

    try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(stmt, params);
      }

      List<BudgetResponse.Budget> results = new ArrayList<>();
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(budRespRowMapper.map(rs));
        }
      }
      return results;
    }
  }
}
