package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.entity.Budget;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BudgetService {
  private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

  private final TransactionManager transactionManager;

  public BudgetService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public BudgetResponse create(String requestId, BudgetRequest budgetRequest) throws SQLException {
    log.debug("[{}] Create budget: BudgetRequest=[{}]", requestId, budgetRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          BudgetDao budgetDao = new BudgetDao(requestId, transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, transactionContext.connection());

          Validations.validateBudget(requestId, budgetRequest, categoryDao);

          Budget budgetIn =
              new Budget(
                  null,
                  budgetRequest.categoryId(),
                  budgetRequest.budgetMonth(),
                  budgetRequest.budgetYear(),
                  budgetRequest.amount(),
                  budgetRequest.notes(),
                  null,
                  null);
          UUID id = budgetDao.create(budgetIn).id();
          log.debug("[{}] Created budget: Id=[{}]", requestId, id);
          BudgetResponse.Budget budget =
              budgetDao.readBudgets(List.of(id), 0, 0, List.of()).getFirst();

          return new BudgetResponse(
              List.of(budget), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public BudgetResponse read(
      String requestId, List<UUID> ids, RequestParams.BudgetParams budgetParams)
      throws SQLException {
    log.debug("[{}] Read budgets:Ids: {}, BudgetParams=[{}]", requestId, ids, budgetParams);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          BudgetDao budgetDao = new BudgetDao(requestId, transactionContext.connection());
          List<BudgetResponse.Budget> budgets =
              budgetDao.readBudgets(
                  ids,
                  budgetParams.budgetMonth(),
                  budgetParams.budgetYear(),
                  budgetParams.categoryIds());

          if (ids.size() == 1 && budgets.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", ids.getFirst().toString());
          }

          return new BudgetResponse(budgets, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public BudgetResponse update(String requestId, UUID id, BudgetRequest budgetRequest)
      throws SQLException {
    log.debug("[{}] Update budget: Id=[{}], BudgetRequest=[{}]", requestId, id, budgetRequest);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          BudgetDao budgetDao = new BudgetDao(requestId, transactionContext.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, transactionContext.connection());
          Validations.validateBudget(requestId, budgetRequest, categoryDao);

          List<Budget> budgets = budgetDao.read(List.of(id));
          if (budgets.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", id.toString());
          }

          Budget budgetIn =
              new Budget(
                  id,
                  budgetRequest.categoryId(),
                  budgetRequest.budgetMonth(),
                  budgetRequest.budgetYear(),
                  budgetRequest.amount(),
                  budgetRequest.notes(),
                  null,
                  null);
          budgetDao.update(budgetIn);
          BudgetResponse.Budget budget =
              budgetDao.readBudgets(List.of(id), 0, 0, List.of()).getFirst();
          return new BudgetResponse(
              List.of(budget), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public BudgetResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete budgets: Ids=[{}]", requestId, ids);
    return transactionManager.execute(
        requestId,
        transactionContext -> {
          BudgetDao budgetDao = new BudgetDao(requestId, transactionContext.connection());

          List<Budget> budgets = budgetDao.read(ids);
          if (ids.size() == 1 && budgets.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", ids.getFirst().toString());
          }

          int deleteCount = budgetDao.delete(ids);
          return new BudgetResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
