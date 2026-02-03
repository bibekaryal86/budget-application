package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.db.dao.DaoFactory;
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
  private final DaoFactory<InsightsDao> insightsDaoFactory;

  public InsightsService(DataSource dataSource, DaoFactory<InsightsDao> insightsDaoFactory) {
    this.transactionManager = new TransactionManager(dataSource);
    this.insightsDaoFactory = insightsDaoFactory;
  }

  public InsightsResponse.CashFlowSummaries readCashFLowSummaries(
      RequestParams.CashFlowSummaryParams requestParams) throws SQLException {
    log.debug("Read cash flow summary: RequestParams=[{}]", requestParams);

    return transactionManager.execute(
        transactionContext -> {
          InsightsDao insightsDao = insightsDaoFactory.create(transactionContext.connection());
          List<InsightsResponse.CashFlowSummary> cashFlowSummaries =
              insightsDao.readCashFlowSummary(
                  requestParams.beginDate(), requestParams.endDate(), requestParams.monthsAgo());
          return new InsightsResponse.CashFlowSummaries(
              cashFlowSummaries, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public InsightsResponse.CategorySummaries readCategoriesSummary(
      RequestParams.CategorySummaryParams requestParams) throws SQLException {
    log.debug("Read categories summary: RequestParams=[{}]", requestParams);

    return transactionManager.execute(
        transactionContext -> {
          InsightsDao insightsDao = insightsDaoFactory.create(transactionContext.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();
          List<UUID> categoryIds = requestParams.categoryIds();
          List<UUID> categoryTypeIds = requestParams.categoryTypeIds();
          int monthsAgo = requestParams.monthsAgo();

          List<InsightsResponse.CategorySummary> categorySummaries =
              insightsDao.readCategorySummary(
                  beginDate, endDate, categoryIds, categoryTypeIds, monthsAgo);

          List<InsightsResponse.CategorySummary> filteredCategorySummaries = categorySummaries;
          if (requestParams.topExpenses() > 0 && !categorySummaries.isEmpty()) {
            InsightsResponse.CategorySummary mostRecentMonth = categorySummaries.getFirst();

            List<InsightsResponse.CategoryAmounts> allCategoryAmounts =
                mostRecentMonth.categoryAmounts();

            List<InsightsResponse.CategoryAmounts> topExpenseCategories =
                allCategoryAmounts.stream()
                    .filter(
                        ca ->
                            !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(
                                ca.category().categoryType().name()))
                    .sorted(
                        Comparator.comparing(InsightsResponse.CategoryAmounts::amount).reversed())
                    .limit(requestParams.topExpenses())
                    .toList();

            filteredCategorySummaries =
                List.of(
                    new InsightsResponse.CategorySummary(
                        mostRecentMonth.yearMonth(), topExpenseCategories));
          }

          return new InsightsResponse.CategorySummaries(
              filteredCategorySummaries, ResponseMetadata.emptyResponseMetadata());
        });
  }
}
