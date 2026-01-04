package budget.application.service.domain;

import budget.application.common.Exceptions;
import budget.application.common.Validations;
import budget.application.db.dao.BudgetDao;
import budget.application.db.dao.CategoryDao;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.RequestParams;
import budget.application.model.entity.Budget;
import budget.application.service.util.ResponseMetadataUtils;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BudgetService {
  private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

  private final TransactionManager tx;

  public BudgetService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public BudgetResponse create(String requestId, BudgetRequest br) throws SQLException {
    log.debug("[{}] Create budget: BudgetRequest=[{}]", requestId, br);
    return tx.execute(
        bs -> {
          BudgetDao dao = new BudgetDao(requestId, bs.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, bs.connection());

          Validations.validateBudget(requestId, br, categoryDao);

          Budget bIn =
              new Budget(
                  null,
                  br.categoryId(),
                  br.budgetMonth(),
                  br.budgetYear(),
                  br.amount(),
                  br.notes(),
                  null,
                  null);
          UUID id = dao.create(bIn).id();
          log.debug("[{}] Created budget: Id=[{}]", requestId, id);
          BudgetResponse.Budget budget = dao.readBudgets(List.of(id), 0, 0, List.of()).getFirst();

          return new BudgetResponse(
              List.of(budget), ResponseMetadataUtils.defaultInsertResponseMetadata());
        });
  }

  public BudgetResponse read(String requestId, List<UUID> ids, RequestParams.BudgetParams params)
      throws SQLException {
    log.debug("[{}] Read budgets:Ids: {}, BudgetParams=[{}]", requestId, ids, params);
    return tx.execute(
        bs -> {
          BudgetDao dao = new BudgetDao(requestId, bs.connection());
          List<BudgetResponse.Budget> budgets =
              dao.readBudgets(ids, params.budgetMonth(), params.budgetYear(), params.catIds());

          if (ids.size() == 1 && budgets.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", ids.getFirst().toString());
          }

          return new BudgetResponse(budgets, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public BudgetResponse update(String requestId, UUID id, BudgetRequest br) throws SQLException {
    log.debug("[{}] Update budget: Id=[{}], BudgetRequest=[{}]", requestId, id, br);
    return tx.execute(
        bs -> {
          BudgetDao dao = new BudgetDao(requestId, bs.connection());
          CategoryDao categoryDao = new CategoryDao(requestId, bs.connection());
          Validations.validateBudget(requestId, br, categoryDao);

          List<Budget> bList = dao.read(List.of(id));
          if (bList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", id.toString());
          }

          Budget bIn =
              new Budget(
                  id,
                  br.categoryId(),
                  br.budgetMonth(),
                  br.budgetYear(),
                  br.amount(),
                  br.notes(),
                  null,
                  null);
          dao.update(bIn);
          BudgetResponse.Budget budget = dao.readBudgets(List.of(id), 0, 0, List.of()).getFirst();
          return new BudgetResponse(
              List.of(budget), ResponseMetadataUtils.defaultUpdateResponseMetadata());
        });
  }

  public BudgetResponse delete(String requestId, List<UUID> ids) throws SQLException {
    log.info("[{}] Delete budgets: Ids=[{}]", requestId, ids);
    return tx.execute(
        bs -> {
          BudgetDao dao = new BudgetDao(requestId, bs.connection());

          List<Budget> aList = dao.read(ids);
          if (ids.size() == 1 && aList.isEmpty()) {
            throw new Exceptions.NotFoundException(requestId, "Budget", ids.getFirst().toString());
          }

          int deleteCount = dao.delete(ids);
          return new BudgetResponse(
              List.of(), ResponseMetadataUtils.defaultDeleteResponseMetadata(deleteCount));
        });
  }
}
