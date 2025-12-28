package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.TestDataHelper;
import budget.application.TestDataSource;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionItemHandlerTest extends IntegrationBaseTest {

  @Test
  void testTransactionItems() throws Exception {
    // CREATE
    TransactionItemRequest req =
        new TransactionItemRequest(TEST_ID, TEST_ID, "Test Item", 100.0, "NEEDS");
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionItemResponse response =
        JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.label().toUpperCase(), response.data().getFirst().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new TransactionItemRequest(TEST_ID, TEST_ID, "Item Test", 100.0, "WANTS");
    resp = httpPut(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.label().toUpperCase(), response.data().getFirst().label());
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
  void testReadTransactionItems() throws Exception {
    // SETUP
    TestDataHelper helper = new TestDataHelper(TestDataSource.getDataSource());
    UUID ctId1 = UUID.randomUUID();
    UUID ctId2 = UUID.randomUUID();
    UUID cId1 = UUID.randomUUID();
    UUID cId2 = UUID.randomUUID();
    UUID cId3 = UUID.randomUUID();
    UUID tId1 = UUID.randomUUID();
    UUID tId2 = UUID.randomUUID();
    UUID tId3 = UUID.randomUUID();
    UUID tiId1 = UUID.randomUUID();
    UUID tiId2 = UUID.randomUUID();
    UUID tiId3 = UUID.randomUUID();

    helper.insertCategoryType(ctId1, "CT ONE");
    helper.insertCategoryType(ctId2, "CT TWO");

    helper.insertCategory(cId1, ctId1, "C ONE");
    helper.insertCategory(cId2, ctId1, "C TWO");
    helper.insertCategory(cId3, ctId2, "C THREE");

    helper.insertTransaction(tId1, LocalDate.now(), 100.00);
    helper.insertTransaction(tId2, LocalDate.now().minusMonths(1L), 200.00);
    helper.insertTransaction(tId3, LocalDate.now().minusMonths(2L), 300.00);

    helper.insertTransactionItem(tiId1, tId1, cId1, 50, "NEEDS");
    helper.insertTransactionItem(tiId2, tId1, cId2, 50, "NEEDS");
    helper.insertTransactionItem(tiId3, tId2, cId2, 200, "INCOME");

    HttpResponse<String> resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    CategoryResponse response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(4, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1 + "?txnIds=" + TEST_ID, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(1, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1 + "?catIds=" + TEST_ID + "," + cId2, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CategoryResponse.class);
    Assertions.assertEquals(3, response.data().size());

    // CLEANUP
    helper.deleteTransactionItem(tiId1);
    helper.deleteTransactionItem(tiId2);
    helper.deleteTransactionItem(tiId3);
    helper.deleteTransaction(tId1);
    helper.deleteTransaction(tId2);
    helper.deleteTransaction(tId3);
    helper.deleteCategory(cId1);
    helper.deleteCategory(cId2);
    helper.deleteCategory(cId3);
    helper.deleteCategoryType(ctId1);
    helper.deleteCategoryType(ctId2);
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

    TransactionItemRequest req = new TransactionItemRequest(null, null, "", 0.0, "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item transaction cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, null, "", 0.0, "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item category cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "", 0.0, "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item label cannot be empty..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "some-label", 0.0, "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("Transaction item amount cannot be zero or negative..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "some-label", 10.0, "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item type cannot be empty..."));

    req = new TransactionItemRequest(TEST_ID, TEST_ID, "some-label", 10.0, "SOMETHING");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item type is invalid..."));

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
    UUID randomId = UUID.randomUUID();
    TransactionItemRequest req =
        new TransactionItemRequest(TEST_ID, TEST_ID, "Item Test", 100.0, "NEEDS");
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
