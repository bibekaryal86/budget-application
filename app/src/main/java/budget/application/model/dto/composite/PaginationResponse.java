package budget.application.model.dto.composite;

import java.util.List;

public record PaginationResponse<T>(List<T> items, int totalCount, int perPage, int pageNumber) {}
