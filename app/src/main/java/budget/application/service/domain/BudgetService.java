package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.DaoFactory;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.entity.Budget;
import budget.application.model.entity.Category;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BudgetService {
  private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

  private final TransactionManager transactionManager;
  private final DaoFactory<BudgetDao> budgetDaoFactory;
  private final CategoryService categoryService;

  public BudgetService(
      DataSource dataSource,
      DaoFactory<BudgetDao> budgetDaoFactory,
      CategoryService categoryService) {
    this.transactionManager = new TransactionManager(dataSource);
    this.budgetDaoFactory = budgetDaoFactory;
    this.categoryService = categoryService;
  }

  public BudgetResponse create(BudgetRequest budgetRequest) throws SQLException {
    log.debug("Create budget: BudgetRequest=[{}]", budgetRequest);
    return transactionManager.execute(
        transactionContext -> {
          BudgetDao budgetDao = budgetDaoFactory.create(transactionContext.connection());
          validateBudget(budgetRequest, transactionContext.connection());

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
          log.debug("Created budget: Id=[{}]", id);
          BudgetResponse.Budget budget =
              budgetDao.readBudgets(List.of(id), 0, 0, List.of()).getFirst();

          return new BudgetResponse(
              List.of(budget), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public BudgetResponse read(List<UUID> ids, RequestParams.BudgetParams budgetParams)
      throws SQLException {
    log.debug("Read budgets:Ids: {}, BudgetParams=[{}]", ids, budgetParams);
    return transactionManager.execute(
        transactionContext -> {
          BudgetDao budgetDao = budgetDaoFactory.create(transactionContext.connection());
          List<BudgetResponse.Budget> budgets =
              budgetDao.readBudgets(
                  ids,
                  budgetParams.budgetMonth(),
                  budgetParams.budgetYear(),
                  budgetParams.categoryIds());

          if (ids.size() == 1 && budgets.isEmpty()) {
            throw new Exceptions.NotFoundException("Budget", ids.getFirst().toString());
          }

          return new BudgetResponse(budgets, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public BudgetResponse update(UUID id, BudgetRequest budgetRequest) throws SQLException {
    log.debug("Update budget: Id=[{}], BudgetRequest=[{}]", id, budgetRequest);
    return transactionManager.execute(
        transactionContext -> {
          BudgetDao budgetDao = budgetDaoFactory.create(transactionContext.connection());
          validateBudget(budgetRequest, transactionContext.connection());

          List<Budget> budgets = budgetDao.read(List.of(id));
          if (budgets.isEmpty()) {
            throw new Exceptions.NotFoundException("Budget", id.toString());
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

  public BudgetResponse delete(List<UUID> ids) throws SQLException {
    log.info("Delete budgets: Ids=[{}]", ids);
    return transactionManager.execute(
        transactionContext -> {
          BudgetDao budgetDao = budgetDaoFactory.create(transactionContext.connection());

          List<Budget> budgets = budgetDao.read(ids);
          if (ids.size() == 1 && budgets.isEmpty()) {
            throw new Exceptions.NotFoundException("Budget", ids.getFirst().toString());
          }

          int deleteCount = budgetDao.delete(ids);
          return new BudgetResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }

  private void validateBudget(BudgetRequest budgetRequest, Connection connection) {
    if (budgetRequest == null) {
      throw new Exceptions.BadRequestException("Budget request cannot be null...");
    }
    if (budgetRequest.categoryId() == null) {
      throw new Exceptions.BadRequestException("Budget category cannot be null...");
    }
    if (budgetRequest.budgetMonth() < 1 || budgetRequest.budgetMonth() > 12) {
      throw new Exceptions.BadRequestException("Budget month should be between 1 and 12...");
    }
    if (budgetRequest.budgetYear() < 2025 || budgetRequest.budgetYear() > 2100) {
      throw new Exceptions.BadRequestException("Budget year should be between 2025 and 2100...");
    }

    if (budgetRequest.amount() == null || budgetRequest.amount().intValue() < 1) {
      throw new Exceptions.BadRequestException("Budget amount cannot be zero or negative...");
    }

    List<Category> categories =
        categoryService.readNoEx(List.of(budgetRequest.categoryId()), connection);

    if (CommonUtilities.isEmpty(categories)) {
      throw new Exceptions.BadRequestException("Category does not exist...");
    }
  }
}
