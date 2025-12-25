package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.dto.response.CategoryResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryHandlerTest extends IntegrationBaseTest {

  @Test
  void testCategories() throws Exception {
    // CREATE
    CategoryRequest req = new CategoryRequest(TEST_ID, "Test One");
    HttpResponse<String> resp =
        httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    CategoryResponse response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.CATEGORIES_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.CATEGORIES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new CategoryRequest(TEST_ID, "One Test");
    resp = httpPut(ApiPaths.CATEGORIES_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name(), response.data().getFirst().name());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.CATEGORIES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.CATEGORIES_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[Category] Not found for"));
  }

  @Test
  void testCategoriesUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORIES_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.CATEGORIES_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.CATEGORIES_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.CATEGORIES_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.CATEGORIES_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testCategoriesBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.CATEGORIES_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category request cannot be null..."));

    CategoryRequest req = new CategoryRequest(null, "");
    resp = httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category type cannot be null..."));

    req = new CategoryRequest(TEST_ID, "");
    resp = httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category name cannot be empty..."));

    req = new CategoryRequest(UUID.randomUUID(), "Something");
    resp = httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category type does not exist..."));

    resp = httpGet(ApiPaths.CATEGORIES_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(
            ApiPaths.CATEGORIES_V1_WITH_ID + UUID.randomUUID() + "/something-else", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.CATEGORIES_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.CATEGORIES_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testCategoriesNotFound() throws Exception {
    String randomId = UUID.randomUUID().toString();
    CategoryRequest req = new CategoryRequest(TEST_ID, "Item Test");
    HttpResponse<String> resp =
        httpPut(ApiPaths.CATEGORIES_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Category] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.CATEGORIES_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Category] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.CATEGORIES_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Category] Not found for [" + randomId + "]"));
  }
}
