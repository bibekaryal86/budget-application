package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.response.TransactionItemResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionItemHandlerTest extends IntegrationBaseTest {

  @Test
  void testTransactionItems() throws Exception {
    // CREATE
    TransactionItemRequest req = new TransactionItemRequest(TEST_ID, TEST_ID, "Test Item", 100.0);
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionItemResponse response =
        JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.label(), response.data().getFirst().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new TransactionItemRequest(TEST_ID, TEST_ID, "Item Test", 100.0);
    resp = httpPut(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.label(), response.data().getFirst().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[TransactionItem] Not found for"));
  }

  @Test
  void testTransactionItemsUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testTransactionItemsBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item request cannot be null..."));

    TransactionItemRequest req = new TransactionItemRequest(null, null, "", 0.0);
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item transaction cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, null, "", 0.0);
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item category cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, null, "", 0.0);
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item category cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "", 0.0);
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item label cannot be empty..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "some-label", 0.0);
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("Transaction item amount cannot be zero or negative..."));

    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(
            ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + UUID.randomUUID() + "/something-else",
            Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testTransactionItemsNotFound() throws Exception {
    String randomId = UUID.randomUUID().toString();
    TransactionItemRequest req = new TransactionItemRequest(TEST_ID, TEST_ID, "Item Test", 100.0);
    HttpResponse<String> resp =
        httpPut(
            ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("[TransactionItem] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("[TransactionItem] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("[TransactionItem] Not found for [" + randomId + "]"));
  }
}
