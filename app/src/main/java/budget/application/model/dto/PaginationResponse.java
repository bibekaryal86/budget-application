package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;

public record PaginationResponse<T>(List<T> items, ResponseMetadata.ResponsePageInfo pageInfo) {}
