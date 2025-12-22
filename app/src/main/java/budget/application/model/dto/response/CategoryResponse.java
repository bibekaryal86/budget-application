package budget.application.model.dto.response;

import budget.application.model.entity.Category;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;

public record CategoryResponse(List<Category> data, ResponseMetadata metadata) {}
