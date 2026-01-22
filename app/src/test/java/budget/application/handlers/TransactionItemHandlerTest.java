package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionItemHandlerTest extends IntegrationBaseTest {

  @AfterEach
  void cleanup() throws SQLException {
    testDataHelper.deleteTransactionItem(List.of(TEST_ID));
  }

  @Test
  void testTransactionItems() throws Exception {
    // CREATE
    TransactionItemRequest req =
        new TransactionItemRequest(
            TEST_ID, TEST_ID, new BigDecimal("100.00"), Collections.emptyList(), "Item Test");
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionItemResponse response =
        JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.notes().toUpperCase(), response.data().getFirst().notes());
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
    req =
        new TransactionItemRequest(
            TEST_ID, TEST_ID, new BigDecimal("100.00"), List.of("Tag One"), "Item Test Updated");
    resp = httpPut(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionItemResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.notes().toUpperCase(), response.data().getFirst().notes());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());
    Assertions.assertEquals(
        req.tags().stream().map(String::toUpperCase).toList(), response.data().getFirst().tags());

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
  void testReadTransactionItemTags() throws Exception {
    testDataHelper.insertTransactionItem(
        UUID.randomUUID(), TEST_ID, TEST_ID, 100.00, List.of("TAG1", "TAG2"));
    testDataHelper.insertTransactionItem(
        UUID.randomUUID(), TEST_ID, TEST_ID, 100.00, List.of("TAG1", "TAG3"));
    HttpResponse<String> resp = httpGet(ApiPaths.TRANSACTION_ITEMS_V1_WITH_TAGS, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    TransactionItemResponse.TransactionItemTags response =
        JsonUtils.fromJson(resp.body(), TransactionItemResponse.TransactionItemTags.class);
    Assertions.assertEquals(4, response.data().size());

    Assertions.assertTrue(response.data().contains("TAG1"));
    Assertions.assertTrue(response.data().contains("TAG2"));
    Assertions.assertTrue(response.data().contains("TAG3"));
    Assertions.assertTrue(response.data().contains("TEST TAG"));
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

    TransactionItemRequest req = new TransactionItemRequest(null, null, null, List.of(), "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item transaction cannot be null..."));

    req = new TransactionItemRequest(TEST_ID, null, null, List.of(), "");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item category cannot be null..."));

    req =
        new TransactionItemRequest(
            TEST_ID, TEST_ID, new BigDecimal("00.00"), List.of(), "some-notes");
    resp = httpPost(ApiPaths.TRANSACTION_ITEMS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(
        resp.body().contains("Transaction item amount cannot be null or negative..."));

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
        new TransactionItemRequest(
            TEST_ID, TEST_ID, new BigDecimal("100.00"), List.of(), "Item Test");
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
