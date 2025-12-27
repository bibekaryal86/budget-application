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

  public CompositeResponse compositeCategories(String requestId, CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite categories: CompositeRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CompositeRepository repo = new CompositeRepository(requestId, bs);
          List<CompositeResponse.CategoryComposite> data = repo.readCompositeCategories(cr);
          return new CompositeResponse(null, data, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CompositeResponse compositeTransactions(String requestId, CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite transactions: CompositeRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CompositeRepository repo = new CompositeRepository(requestId, bs);
          List<CompositeResponse.TransactionComposite> data =
              repo.readCompositeTransactions(normalizeCompositeTransactionRequest(cr));
          return new CompositeResponse(data, null, ResponseMetadata.emptyResponseMetadata());
        });
  }

  private CompositeRequest normalizeCompositeTransactionRequest(CompositeRequest cr) {
    LocalDate now = LocalDate.now();
    YearMonth currentMonth = YearMonth.from(now);

    LocalDate defaultStart = currentMonth.atDay(1);
    LocalDate defaultEnd = currentMonth.atEndOfMonth();

    CompositeRequest.TransactionRequest tr = (cr == null ? null : cr.transactionRequest());

    if (tr == null) {
      return new CompositeRequest(
          new CompositeRequest.TransactionRequest(defaultStart, defaultEnd, null, null, null),
          null);
    }

    LocalDate begin = tr.beginDate();
    LocalDate end = tr.endDate();

    // (1) Both missing → use current month
    if (begin == null && end == null) {
      begin = defaultStart;
      end = defaultEnd;
    }
    // (2) begin provided, end missing → end = end of begin's month
    else if (begin != null && end == null) {
      YearMonth ym = YearMonth.from(begin);
      end = ym.atEndOfMonth();
    }
    // (3) end provided, begin missing → begin = start of end's month
    else if (begin == null && end != null) {
      YearMonth ym = YearMonth.from(end);
      begin = ym.atDay(1);
    }

    return new CompositeRequest(
        new CompositeRequest.TransactionRequest(
            begin, end, tr.merchant(), tr.categoryId(), tr.categoryTypeId()),
        null);
  }
}
