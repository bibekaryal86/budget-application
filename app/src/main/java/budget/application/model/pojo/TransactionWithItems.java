package budget.application.model.pojo;

import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import java.util.List;
import lombok.Builder;

@Builder
public record TransactionWithItems(Transaction transaction, List<TransactionItem> items) {}
