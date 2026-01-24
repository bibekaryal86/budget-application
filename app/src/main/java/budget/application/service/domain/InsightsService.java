package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.db.dao.InsightsDao;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.dto.RequestParams;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightsService {
  private static final Logger log = LoggerFactory.getLogger(InsightsService.class);

  private final TransactionManager tx;

  public InsightsService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public InsightsResponse.CashFlowSummaries readCashFLowSummaries(
      String requestId, RequestParams.CashFlowSummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read cash flow summary: RequestParams=[{}]", requestId, requestParams);

    return tx.execute(
        bs -> {
          InsightsDao dao = new InsightsDao(requestId, bs.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();

          LocalDate prevBegin = beginDate.minusMonths(1);
          LocalDate prevEnd = endDate.minusMonths(1);

          InsightsResponse.CashFlowSummary currentMonth =
              dao.readCashFlowSummary(beginDate, endDate);
          InsightsResponse.CashFlowSummary prevMonth =
              dao.readCashFlowSummary(prevBegin, prevEnd);

          return new InsightsResponse.CashFlowSummaries(
              currentMonth, prevMonth, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public InsightsResponse.CategorySummaries readCategoriesSummary(
      String requestId, RequestParams.CategorySummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read categories summary: RequestParams=[{}]", requestId, requestParams);

    return tx.execute(
        bs -> {
          InsightsDao dao = new InsightsDao(requestId, bs.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();
          List<UUID> catIds = requestParams.catIds();
          List<UUID> catTypeIds = requestParams.catTypeIds();

          List<InsightsResponse.CategorySummary> categorySummaries =
              dao.readCategorySummary(beginDate, endDate, catIds, catTypeIds);

          List<InsightsResponse.CategoryTypeSummary> categoryTypeSummaries =
              categorySummaries.stream()
                  .collect(
                      Collectors.groupingBy(
                          cs -> cs.category().categoryType(),
                          Collectors.reducing(
                              BigDecimal.ZERO,
                              InsightsResponse.CategorySummary::amount,
                              BigDecimal::add)))
                  .entrySet()
                  .stream()
                  .map(e -> new InsightsResponse.CategoryTypeSummary(e.getKey(), e.getValue()))
                  .toList();

          if (requestParams.topExpenses()) {
              categorySummaries = categorySummaries.stream()
                      .filter(cs -> !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(cs.category().categoryType().name()))
                      .sorted(Comparator.comparing(InsightsResponse.CategorySummary::amount).reversed())
                      .limit(10)
                      .toList();

              categoryTypeSummaries = categoryTypeSummaries.stream()
                      .filter(cts -> !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(cts.categoryType().name()))
                      .sorted(Comparator.comparing(InsightsResponse.CategoryTypeSummary::amount).reversed())
                      .limit(10)
                      .toList();
          }

            return new InsightsResponse.CategorySummaries(
              beginDate,
              endDate,
              categorySummaries,
              categoryTypeSummaries,
              ResponseMetadata.emptyResponseMetadata());
        });
  }
}
