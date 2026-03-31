package budget.application.event;

import budget.application.model.dto.TransactionResponse;
import java.util.List;

public record TransactionEvent(
    String mdcRequestId,
    Type eventType,
    List<TransactionResponse.Transaction> transactionResponse,
    List<TransactionResponse.Transaction> transactionResponseBeforeUpdate) {

  public enum Type {
    CREATE,
    UPDATE,
    DELETE
  }
}
