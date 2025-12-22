package budget.application.model.dto.composite;

import budget.application.model.entity.Transaction;
import budget.application.model.entity.TransactionItem;
import java.util.List;

public record TransactionWithItems(Transaction transaction, List<TransactionItem> items) {}
