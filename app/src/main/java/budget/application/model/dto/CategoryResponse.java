package budget.application.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;
import java.util.UUID;

public record CategoryResponse(List<Category> data, ResponseMetadata metadata) {
  public record Category(UUID id, CategoryTypeResponse.CategoryType categoryType, String name) {}
}
