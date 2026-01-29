package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryHandlerTest extends IntegrationBaseTest {

  @AfterEach
  void cleanup() throws SQLException {
    testDataHelper.deleteCategory(List.of(TEST_ID));
  }

  @Test
  void testCategories() throws Exception {
    // CREATE
    CategoryRequest req = new CategoryRequest(TEST_ID, "Test One");
    HttpResponse<String> resp =
        httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    CategoryResponse response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
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
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
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
  void testReadCategories() throws Exception {
    // SETUP
    UUID ctId1 = UUID.randomUUID();
    UUID ctId2 = UUID.randomUUID();
    UUID cId1 = UUID.randomUUID();
    UUID cId2 = UUID.randomUUID();
    UUID cId3 = UUID.randomUUID();
    testDataHelper.insertCategoryType(ctId1, "CT ONE");
    testDataHelper.insertCategoryType(ctId2, "CT TWO");
    testDataHelper.insertCategory(cId1, ctId1, "C ONE");
    testDataHelper.insertCategory(cId2, ctId1, "C TWO");
    testDataHelper.insertCategory(cId3, ctId2, "C THREE");

    HttpResponse<String> resp = httpGet(ApiPaths.CATEGORIES_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    CategoryResponse response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(4, response.data().size());

    resp = httpGet(ApiPaths.CATEGORIES_V1 + "?categoryTypeIds=" + TEST_ID, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());

    resp =
        httpGet(ApiPaths.CATEGORIES_V1 + "?categoryTypeIds=" + TEST_ID + "," + ctId1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(3, response.data().size());
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

    req = new CategoryRequest(TEST_ID, "TEST CATEGORY");
    resp = httpPost(ApiPaths.CATEGORIES_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("(" + TEST_ID + ", TEST CATEGORY) already exists."));

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
    UUID randomId = UUID.randomUUID();
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
