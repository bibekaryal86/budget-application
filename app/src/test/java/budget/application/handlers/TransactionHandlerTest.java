package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.TestDataHelper;
import budget.application.TestDataSource;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.net.http.HttpResponse;
import java.time.LocalDate;
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
                new TransactionItemRequest(null, TEST_ID, "Test Label1", 50.00, "NEEDS"),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00, "NEEDS")));
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(
        req.merchant().toUpperCase(), response.data().getFirst().merchant());
    Assertions.assertTrue(
        CommonUtilities.isEmpty(response.data().getFirst().notes()));
    Assertions.assertEquals(
        req.items().getFirst().label().toUpperCase(),
        response.data().getFirst().items().getFirst().label());
    Assertions.assertEquals(
        req.items().getLast().label().toUpperCase(),
        response.data().getFirst().items().getLast().label());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

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
    Assertions.assertEquals(id, response.data().getFirst().id().toString());
    Assertions.assertEquals(
        req.merchant().toUpperCase(), response.data().getFirst().merchant());
    Assertions.assertEquals(
        req.items().getFirst().label().toUpperCase(),
        response.data().getFirst().items().getFirst().label());
    Assertions.assertEquals(
        req.items().getLast().label().toUpperCase(),
        response.data().getFirst().items().getLast().label());

    // UPDATE
    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Merchant Test",
            100.00,
            "Txn Note",
            List.of(
                new TransactionItemRequest(null, TEST_ID, "Label1 Test", 50.00, "NEEDS"),
                new TransactionItemRequest(null, TEST_ID, "Test Label2", 50.00, "NEEDS")));
    resp = httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(
        req.merchant().toUpperCase(), response.data().getFirst().merchant());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().getFirst().notes()));
    Assertions.assertEquals(
        req.notes().toUpperCase(), response.data().getFirst().notes());
    Assertions.assertEquals(
        req.items().getFirst().label().toUpperCase(),
        response.data().getFirst().items().getFirst().label());
    Assertions.assertEquals(
        req.items().getLast().label().toUpperCase(),
        response.data().getFirst().items().getLast().label());
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
  void testTransactionMerchants() throws Exception {
    TestDataHelper helper = new TestDataHelper(TestDataSource.getDataSource());
    UUID txnId1 = UUID.randomUUID();
    UUID txnId2 = UUID.randomUUID();
    UUID txnId3 = UUID.randomUUID();
    helper.insertTransaction(txnId1, LocalDate.now(), 100.00);
    helper.insertTransaction(txnId2, LocalDate.now(), 100.00);
    helper.insertTransaction(txnId3, LocalDate.now(), 100.00);

    HttpResponse<String> resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_MERCHANTS, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(4, response.data().size());

    // does not return transaction items
    Assertions.assertEquals(0, response.data().getFirst().items().size());
    Assertions.assertEquals(0, response.data().get(1).items().size());
    Assertions.assertEquals(0, response.data().get(2).items().size());
    Assertions.assertEquals(0, response.data().getLast().items().size());

    // all fields except merchant are null
    Assertions.assertNull(response.data().getFirst().id());
    Assertions.assertNull(response.data().getFirst().txnDate());
    Assertions.assertNull(response.data().getFirst().notes());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().getFirst().merchant()));

    Assertions.assertNull(response.data().get(1).txnDate());
    Assertions.assertNull(response.data().get(1).notes());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().get(1).merchant()));

    Assertions.assertNull(response.data().get(2).txnDate());
    Assertions.assertNull(response.data().get(2).notes());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().get(2).merchant()));

    Assertions.assertNull(response.data().getLast().txnDate());
    Assertions.assertNull(response.data().getLast().notes());
    Assertions.assertFalse(
        CommonUtilities.isEmpty(response.data().getLast().merchant()));

    helper.deleteTransaction(txnId1);
    helper.deleteTransaction(txnId2);
    helper.deleteTransaction(txnId3);
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
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label1", 40.00, "NEEDS"),
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label2", 50.00, "NEEDS")));
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
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label1", 50, "NEEDS"),
                new TransactionItemRequest(
                    TEST_ID, UUID.randomUUID(), "Test Label2", 50.00, "WANTS")));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category type does not exist..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label1", 50, "NEEDS"),
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label2", 50.00, null)));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item type cannot be empty..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label1", 50, "NEEDS"),
                new TransactionItemRequest(TEST_ID, TEST_ID, "Test Label2", 50.00, "SOMETHING")));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction item type is invalid..."));

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
    UUID randomId = UUID.randomUUID();
    TransactionRequest req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Test Merchant",
            100.00,
            "",
            List.of(
                new TransactionItemRequest(randomId, TEST_ID, "Test Label1", 50.00, "NEEDS"),
                new TransactionItemRequest(randomId, TEST_ID, "Test Label2", 50.00, "WANTS")));
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
