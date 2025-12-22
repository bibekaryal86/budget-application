package budget.application.model.dto.response;

import budget.application.model.dto.composite.TransactionWithItems;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;

public record TransactionResponse(List<TransactionWithItems> data, ResponseMetadata metadata) {}
