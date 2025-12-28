package budget.application.model.dto;

import budget.application.model.entity.TransactionItem;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;

public record TransactionItemResponse(List<TransactionItem> data, ResponseMetadata metadata) {}
