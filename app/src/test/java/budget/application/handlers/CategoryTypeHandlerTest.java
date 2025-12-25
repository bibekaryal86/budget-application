package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.response.CategoryTypeResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryTypeHandlerTest extends IntegrationBaseTest {

  @Test
  void testCategoryTypes() throws Exception {
    // CREATE
    CategoryTypeRequest req = new CategoryTypeRequest("Test One");
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORY_TYPES_V1, JsonUtils.toJson(req));
    Assertions.assertEquals(201, resp.statusCode());
    CategoryTypeResponse response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new CategoryTypeRequest("One Test");
    resp = httpPut(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id, JsonUtils.toJson(req));
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[CategoryType] Not found for"));
  }
}
