package budget.application.service.domain;

import budget.application.db.dao.CompositeDao;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import budget.application.service.util.TransactionManager;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeService {
  private static final Logger log = LoggerFactory.getLogger(CompositeService.class);

  private final TransactionManager tx;

  public CompositeService(DataSource dataSource) {
    this.tx = new TransactionManager(dataSource);
  }

  public CompositeResponse compositeCategories(String requestId, CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite categories: CompositeRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CompositeDao dao = new CompositeDao(requestId, bs.connection());
          List<CompositeResponse.CategoryComposite> data = dao.compositeCategories(cr);
          return new CompositeResponse(null, data, ResponseMetadata.emptyResponseMetadata());
        });
  }

  public CompositeResponse compositeTransactions(String requestId, CompositeRequest cr)
      throws SQLException {
    log.debug("[{}] Composite transactions: CompositeRequest=[{}]", requestId, cr);
    return tx.execute(
        bs -> {
          CompositeDao dao = new CompositeDao(requestId, bs.connection());
          List<CompositeResponse.TransactionComposite> data =
              dao.compositeTransactions(normalizeCompositeTransactionRequest(cr));
          return new CompositeResponse(data, null, ResponseMetadata.emptyResponseMetadata());
        });
  }

  private CompositeRequest normalizeCompositeTransactionRequest(CompositeRequest cr) {
    LocalDate now = LocalDate.now();
    YearMonth currentMonth = YearMonth.from(now);

    LocalDate defaultStart = currentMonth.atDay(1);
    LocalDate defaultEnd = currentMonth.atEndOfMonth();

    CompositeRequest.TransactionComposite crtc = (cr == null ? null : cr.transactionComposite());

    if (crtc == null) {
      return new CompositeRequest(
          new CompositeRequest.TransactionComposite(
              defaultStart, defaultEnd, List.of(), List.of(), List.of(), List.of()),
          null);
    }

    LocalDate begin = crtc.beginDate();
    LocalDate end = crtc.endDate();

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
        new CompositeRequest.TransactionComposite(
            begin, end, crtc.merchants(), crtc.catIds(), crtc.catTypeIds(), crtc.txnTypes()),
        null);
  }
}
