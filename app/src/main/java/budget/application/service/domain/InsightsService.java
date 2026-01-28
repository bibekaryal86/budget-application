package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.db.dao.InsightsDao;
import budget.application.db.util.TransactionManager;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.dto.RequestParams;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightsService {
  private static final Logger log = LoggerFactory.getLogger(InsightsService.class);

  private final TransactionManager transactionManager;

  public InsightsService(DataSource dataSource) {
    this.transactionManager = new TransactionManager(dataSource);
  }

  public InsightsResponse.CashFlowSummaries readCashFLowSummaries(
      String requestId, RequestParams.CashFlowSummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read cash flow summary: RequestParams=[{}]", requestId, requestParams);

    return transactionManager.execute(
        requestId,
        transactionContext -> {
          InsightsDao insightsDao = new InsightsDao(requestId, transactionContext.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();

          LocalDate previousMonthBeginDate = beginDate.minusMonths(1);
          LocalDate previousMonthEndDate = endDate.minusMonths(1);

          InsightsResponse.CashFlowSummary currentMonth =
              insightsDao.readCashFlowSummary(beginDate, endDate);
          InsightsResponse.CashFlowSummary prevMonth =
              insightsDao.readCashFlowSummary(previousMonthBeginDate, previousMonthEndDate);

          return new InsightsResponse.CashFlowSummaries(
              currentMonth, prevMonth, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public InsightsResponse.CategorySummaries readCategoriesSummary(
      String requestId, RequestParams.CategorySummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read categories summary: RequestParams=[{}]", requestId, requestParams);

    return transactionManager.execute(
        requestId,
        transactionContext -> {
          InsightsDao insightsDao = new InsightsDao(requestId, transactionContext.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();
          List<UUID> categoryIds = requestParams.catIds();
          List<UUID> categoryTypeIds = requestParams.catTypeIds();

          List<InsightsResponse.CategorySummary> currentMonth =
              insightsDao.readCategorySummary(beginDate, endDate, categoryIds, categoryTypeIds);

          List<InsightsResponse.CategorySummary> filteredCurrentMonth = currentMonth;
          if (requestParams.topExpenses()) {
            filteredCurrentMonth =
                currentMonth.stream()
                    .filter(
                        cs ->
                            !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(
                                cs.category().categoryType().name()))
                    .sorted(
                        Comparator.comparing(InsightsResponse.CategorySummary::amount).reversed())
                    .limit(7)
                    .toList();
          }

          LocalDate previousMonthBeginDate = beginDate.minusMonths(1);
          LocalDate previousMonthEndDate = endDate.minusMonths(1);
          List<UUID> relevantCatIds =
              filteredCurrentMonth.stream().map(cs -> cs.category().id()).toList();

          List<InsightsResponse.CategorySummary> previousMonth =
              insightsDao.readCategorySummary(
                  previousMonthBeginDate, previousMonthEndDate, relevantCatIds, List.of());

          return new InsightsResponse.CategorySummaries(
              filteredCurrentMonth, previousMonth, ResponseMetadata.emptyResponseMetadata());
        });
  }
}
