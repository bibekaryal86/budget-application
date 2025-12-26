package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.request.TransactionItemRequest;
import budget.application.model.dto.request.TransactionRequest;
import budget.application.model.dto.response.TransactionResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionHandlerTest extends IntegrationBaseTest {

  @Test
  void testTransactions() throws Exception {
    // CREATE
    TransactionRequest req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Test Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Test Label1", 50.00),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00)));
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(req.merchant(), response.data().getFirst().transaction().merchant());
    Assertions.assertTrue(
        CommonUtilities.isEmpty(response.data().getFirst().transaction().notes()));
    Assertions.assertEquals(
        req.items().getFirst().label(), response.data().getFirst().items().getFirst().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().transaction().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.TRANSACTIONS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(id, response.data().getFirst().transaction().id().toString());
    Assertions.assertEquals(req.merchant(), response.data().getFirst().transaction().merchant());
    Assertions.assertEquals(
        req.items().getFirst().label(), response.data().getFirst().items().getFirst().label());

    // UPDATE
    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Merchant Test",
            100.00,
            "Txn Note",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Label1 Test", 50.00),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00)));
    resp = httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(req.merchant(), response.data().getFirst().transaction().merchant());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().getFirst().transaction().notes()));
    Assertions.assertEquals(req.notes(), response.data().getFirst().transaction().notes());
    Assertions.assertEquals(
        req.items().getFirst().label(), response.data().getFirst().items().getFirst().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[Transaction] Not found for"));
  }

  @Test
  void testTransactionsUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.TRANSACTIONS_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.TRANSACTIONS_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.TRANSACTIONS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testTransactionsBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.TRANSACTIONS_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction request cannot be null..."));

    TransactionRequest req = new TransactionRequest(LocalDateTime.now(), "", 100.00, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction merchant cannot be empty..."));

    req = new TransactionRequest(LocalDateTime.now(), "Some Merchant", 0.00, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction total cannot be negative..."));

    req = new TransactionRequest(LocalDateTime.now(), "Some Merchant", 100.00, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction must have at least one item..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Test Label1", 40.00),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00)));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Total amount does not match sum of items..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Test Label1", 50),
                new TransactionItemRequest(null, UUID.randomUUID(), "Test Label2", 50.00)));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("One or more category IDs do not exist..."));

    resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(
            ApiPaths.TRANSACTIONS_V1_WITH_ID + UUID.randomUUID() + "/something-else", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.TRANSACTIONS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testTransactionsNotFound() throws Exception {
    String randomId = UUID.randomUUID().toString();
    TransactionRequest req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Test Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Test Label1", 50.00),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00)));
    HttpResponse<String> resp =
        httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Transaction] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Transaction] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.TRANSACTIONS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Transaction] Not found for [" + randomId + "]"));
  }
}
