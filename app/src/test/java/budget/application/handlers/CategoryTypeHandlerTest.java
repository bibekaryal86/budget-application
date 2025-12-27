package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.response.CategoryTypeResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryTypeHandlerTest extends IntegrationBaseTest {

  @Test
  void testCategoryTypes() throws Exception {
    // CREATE
    CategoryTypeRequest req = new CategoryTypeRequest("Test One");
    HttpResponse<String> resp =
        httpPost(ApiPaths.CATEGORY_TYPES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    CategoryTypeResponse response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new CategoryTypeRequest("One Test");
    resp = httpPut(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryTypeResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[CategoryType] Not found for"));
  }

  @Test
  void testCategoryTypesUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORY_TYPES_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testCategoryTypesBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORY_TYPES_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category type request cannot be null..."));

    CategoryTypeRequest req = new CategoryTypeRequest("");
    resp = httpPost(ApiPaths.CATEGORY_TYPES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category type name cannot be empty..."));

    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(
            ApiPaths.CATEGORY_TYPES_V1_WITH_ID + UUID.randomUUID() + "/something-else",
            Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testCategoryTypesNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();
    CategoryTypeRequest req = new CategoryTypeRequest("New Name");
    HttpResponse<String> resp =
        httpPut(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[CategoryType] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[CategoryType] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[CategoryType] Not found for [" + randomId + "]"));
  }

  @Test
  void testCategoryTypesDuplicateError() throws Exception {
    String req = JsonUtils.toJson(new CategoryTypeRequest("Test Category Type"));
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORY_TYPES_V1, req, Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    ResponseWithMetadata response = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        response
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("duplicate key value violates unique constraint"));
  }

  @Test
  void testCategoryTypesDeleteError() throws Exception {
    HttpResponse<String> resp =
        httpDelete(ApiPaths.CATEGORY_TYPES_V1_WITH_ID + TEST_ID, Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    ResponseWithMetadata response = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        response
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("is still referenced from table"));
  }
}
