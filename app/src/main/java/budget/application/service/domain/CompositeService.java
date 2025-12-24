package budget.application.service.domain;

import budget.application.db.repository.CompositeRepository;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompositeService {

  private final TransactionManager tx;

  public CompositeService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public CompositeResponse compositeTransactions(String requestId, CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite transactions: CompositeRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CompositeRepository repo = new CompositeRepository(bs);
          List<CompositeResponse.TransactionComposite> data =
              repo.read(normalizeCompositeTransactionRequest(cr));
          return new CompositeResponse(data, ResponseMetadata.emptyResponseMetadata());
        });
  }

  private CompositeRequest normalizeCompositeTransactionRequest(CompositeRequest cr) {
    LocalDate now = LocalDate.now();
    YearMonth currentMonth = YearMonth.from(now);

    LocalDate monthStart = currentMonth.atDay(1);
    LocalDate monthEnd = currentMonth.atEndOfMonth();

    CompositeRequest.TransactionRequest tr = cr.transactionRequest();

    if (tr == null) {
      return new CompositeRequest(
          new CompositeRequest.TransactionRequest(monthStart, monthEnd, null, null, null));
    }

    LocalDate beginDate = tr.beginDate() != null ? tr.beginDate() : monthStart;
    LocalDate endDate = tr.endDate() != null ? tr.endDate() : monthEnd;

    return new CompositeRequest(
        new CompositeRequest.TransactionRequest(
            beginDate, endDate, tr.merchant(), tr.categoryId(), tr.categoryTypeId()));
  }
}
