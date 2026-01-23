package budget.application.service.domain;

import budget.application.common.Constants;
import budget.application.db.dao.ReportDao;
import budget.application.model.dto.ReportResponse;
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

public class ReportService {
  private static final Logger log = LoggerFactory.getLogger(ReportService.class);

  private final TransactionManager tx;

  public ReportService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public ReportResponse.TransactionSummaries readTransactionsSummary(
      String requestId, RequestParams.TransactionSummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read transactions summary: RequestParams=[{}]", requestId, requestParams);

    return tx.execute(
        bs -> {
          ReportDao dao = new ReportDao(requestId, bs.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();

          LocalDate prevBegin = beginDate.minusMonths(1);
          LocalDate prevEnd = endDate.minusMonths(1);

          ReportResponse.TransactionSummary currentMonth =
              dao.readTransactionSummary(beginDate, endDate);
          ReportResponse.TransactionSummary prevMonth =
              dao.readTransactionSummary(prevBegin, prevEnd);

          return new ReportResponse.TransactionSummaries(
              currentMonth, prevMonth, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public ReportResponse.CategorySummaries readCategoriesSummary(
      String requestId, RequestParams.CategorySummaryParams requestParams) throws SQLException {
    log.debug("[{}] Read categories summary: RequestParams=[{}]", requestId, requestParams);

    return tx.execute(
        bs -> {
          ReportDao dao = new ReportDao(requestId, bs.connection());

          LocalDate beginDate = requestParams.beginDate();
          LocalDate endDate = requestParams.endDate();
          List<UUID> catIds = requestParams.catIds();
          List<UUID> catTypeIds = requestParams.catTypeIds();

          List<ReportResponse.CategorySummary> categorySummaries =
              dao.readCategorySummary(beginDate, endDate, catIds, catTypeIds);

          List<ReportResponse.CategoryTypeSummary> categoryTypeSummaries =
              categorySummaries.stream()
                  .collect(
                      Collectors.groupingBy(
                          cs -> cs.category().categoryType(),
                          Collectors.reducing(
                              BigDecimal.ZERO,
                              ReportResponse.CategorySummary::amount,
                              BigDecimal::add)))
                  .entrySet()
                  .stream()
                  .map(e -> new ReportResponse.CategoryTypeSummary(e.getKey(), e.getValue()))
                  .toList();

          if (requestParams.topExpenses()) {
              categorySummaries = categorySummaries.stream()
                      .filter(cs -> !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(cs.category().categoryType().name()))
                      .sorted(Comparator.comparing(ReportResponse.CategorySummary::amount).reversed())
                      .limit(10)
                      .toList();

              categoryTypeSummaries = categoryTypeSummaries.stream()
                      .filter(cts -> !Constants.NO_EXPENSE_CATEGORY_TYPES.contains(cts.categoryType().name()))
                      .sorted(Comparator.comparing(ReportResponse.CategoryTypeSummary::amount).reversed())
                      .limit(10)
                      .toList();
          }

            return new ReportResponse.CategorySummaries(
              beginDate,
              endDate,
              categorySummaries,
              categoryTypeSummaries,
              ResponseMetadata.emptyResponseMetadata());
        });
  }
}
