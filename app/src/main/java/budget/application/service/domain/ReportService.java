package budget.application.service.domain;

import budget.application.db.dao.ReportDao;
import budget.application.model.dto.ReportResponse;
import budget.application.model.dto.RequestParams;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportService {
  private static final Logger log = LoggerFactory.getLogger(ReportService.class);

  private final TransactionManager tx;

  public ReportService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public ReportResponse readTransactionsSummary(
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

          return new ReportResponse(
              new ReportResponse.TransactionSummaries(currentMonth, prevMonth),
              ResponseMetadata.emptyResponseMetadata());
        });
  }
}
