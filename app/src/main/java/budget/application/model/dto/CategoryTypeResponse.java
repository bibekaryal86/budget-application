package budget.application.model.dto;

import budget.application.model.entity.CategoryType;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;

public record CategoryTypeResponse(List<CategoryType> data, ResponseMetadata metadata) {}
