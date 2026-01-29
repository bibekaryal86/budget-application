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

  private final BudgetRowMappers.BudgetRowMapperResponse budgetRowMapperResponse;

  public BudgetDao(Connection connection) {
    super(connection, new BudgetRowMappers.BudgetRowMapper());
    this.budgetRowMapperResponse = new BudgetRowMappers.BudgetRowMapperResponse();
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
  protected UUID getId(Budget budget) {
    return budget.id();
  }

  @Override
  protected String orderByClause() {
    return "budget_year, budget_month DESC";
  }

  public List<BudgetResponse.Budget> readBudgets(
      List<UUID> ids, int budgetMonth, int budgetYear, List<UUID> categoryIds) throws SQLException {
    log.debug(
        "Read Budgets: Ids={}, Month=[{}], Year=[{}], CategoryIds={}",
        ids,
        budgetMonth,
        budgetYear,
        categoryIds);
    StringBuilder sql =
        new StringBuilder(
            """
                        SELECT
                            b.id           AS budget_id,
                            b.budget_month     AS budget_month,
                            b.budget_year     AS budget_year,
                            b.amount AS budget_amount,
                            b.notes        AS budget_notes,
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
    if (!CommonUtilities.isEmpty(categoryIds)) {
      addWhere.accept("b.category_id IN (" + DaoUtils.placeholders(categoryIds.size()) + ")");
      params.addAll(categoryIds);
    }
    sql.append(" ORDER BY ct.name, c.name ASC");

    log.debug("Read Budgets SQL=[{}]", sql);

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
      if (!params.isEmpty()) {
        DaoUtils.bindParams(preparedStatement, params, Boolean.TRUE);
      }

      List<BudgetResponse.Budget> results = new ArrayList<>();
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          results.add(budgetRowMapperResponse.map(resultSet));
        }
      }
      return results;
    }
  }
}
