package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionHandlerTest extends IntegrationBaseTest {

  @AfterEach
  void cleanup() throws SQLException {
    testDataHelper.deleteTransactionItem(List.of(TEST_ID));
    testDataHelper.deleteTransaction(List.of(TEST_ID));
    testDataHelper.deleteCategory(List.of(TEST_ID));
    testDataHelper.deleteCategoryType(List.of(TEST_ID));
  }

  @Test
  void testTransactions() throws Exception {
    // CREATE
    TransactionRequest req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Test Merchant",
            TEST_ID,
            new BigDecimal("100.00"),
            "",
            List.of(
                new TransactionItemRequest(
                    null, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    null, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 2")));
    HttpResponse<String> resp =
        httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(req.merchant().toUpperCase(), response.data().getFirst().merchant());
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
    Assertions.assertEquals(req.merchant().toUpperCase(), response.data().getFirst().merchant());

    // UPDATE
    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Merchant Test",
            TEST_ID,
            new BigDecimal("100.00"),
            "Txn Note",
            List.of(
                new TransactionItemRequest(
                    null, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    null, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 2")));
    resp = httpPut(ApiPaths.TRANSACTIONS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().items().size());
    Assertions.assertEquals(req.merchant().toUpperCase(), response.data().getFirst().merchant());
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
  void testReadTransactions() throws Exception {
    // SETUP
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

    testDataHelper.insertCategoryType(ctId1, "CT ONE");
    testDataHelper.insertCategoryType(ctId2, "CT TWO");

    testDataHelper.insertCategory(cId1, ctId1, "C ONE");
    testDataHelper.insertCategory(cId2, ctId1, "C TWO");
    testDataHelper.insertCategory(cId3, ctId2, "C THREE");

    testDataHelper.insertTransaction(tId1, LocalDateTime.now(), 100.00);
    testDataHelper.insertTransaction(tId2, LocalDateTime.now().minusMonths(1L), 200.00);
    testDataHelper.insertTransaction(tId3, LocalDateTime.now().minusMonths(2L), 300.00);

    testDataHelper.insertTransactionItem(tiId1, tId1, cId1, 50, List.of("TAG ONE", "TAG TWO"));
    testDataHelper.insertTransactionItem(tiId2, tId1, cId2, 50, List.of("TAG THREE", "TAG FOUR"));
    testDataHelper.insertTransactionItem(tiId3, tId2, cId2, 200, List.of("TAG FIVE", "TAG ONE"));

    HttpResponse<String> resp = httpGet(ApiPaths.TRANSACTIONS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(4, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTIONS_V1 + "?catTypeIds=" + TEST_ID + "," + ctId1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(3, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTIONS_V1 + "?merchants=TEST%20MERCHANT", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTIONS_V1 + "?catIds=" + TEST_ID + "," + cId2, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(3, response.data().size());

    resp = httpGet(ApiPaths.TRANSACTIONS_V1 + "?tags=TAG%20ONE,TAG%20THREE", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(2, response.data().size());

    LocalDate beginDate = LocalDate.of(2025, 1, 1);
    LocalDate endDate = LocalDateTime.now().toLocalDate();
    resp =
        httpGet(
            ApiPaths.TRANSACTIONS_V1
                + "?accIds="
                + TEST_ID
                + "&catIds="
                + TEST_ID
                + ","
                + cId2
                + "&catTypeIds="
                + TEST_ID
                + "&merchants=TEST%20MERCHANT,SOME_MERCHANT"
                + "&tags=TEST%20TAG,SOME_TAG"
                + "&beginDate="
                + beginDate
                + "&endDate="
                + endDate,
            Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
  }

  @Test
  void testReadTransactionMerchants() throws Exception {
    UUID txnId1 = UUID.randomUUID();
    UUID txnId2 = UUID.randomUUID();
    UUID txnId3 = UUID.randomUUID();
    testDataHelper.insertTransaction(txnId1, LocalDateTime.now(), 100.00);
    testDataHelper.insertTransaction(txnId2, LocalDateTime.now(), 100.00);
    testDataHelper.insertTransaction(txnId3, LocalDateTime.now(), 100.00);

    HttpResponse<String> resp = httpGet(ApiPaths.TRANSACTIONS_V1_WITH_MERCHANTS, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    TransactionResponse.TransactionMerchants response =
        JsonUtils.fromJson(resp.body(), TransactionResponse.TransactionMerchants.class);
    Assertions.assertEquals(4, response.data().size());

    Assertions.assertTrue(response.data().contains("TEST MERCHANT"));
    Assertions.assertTrue(response.data().contains("Merchant: " + txnId1));
    Assertions.assertTrue(response.data().contains("Merchant: " + txnId2));
    Assertions.assertTrue(response.data().contains("Merchant: " + txnId3));
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

    TransactionRequest req =
        new TransactionRequest(LocalDateTime.now(), "", null, null, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction merchant cannot be empty..."));

    req = new TransactionRequest(LocalDateTime.now(), "Some Merchant", null, null, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction account cannot be null..."));

    req =
        new TransactionRequest(LocalDateTime.now(), "Some Merchant", TEST_ID, null, "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction total cannot be null or negative..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(), "Some Merchant", TEST_ID, new BigDecimal("0.00"), "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction total cannot be null or negative..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(), "Some Merchant", TEST_ID, new BigDecimal("100.00"), "", List.of());
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Transaction must have at least one item..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            TEST_ID,
            new BigDecimal("100.00"),
            "",
            List.of(
                new TransactionItemRequest(
                    TEST_ID, TEST_ID, new BigDecimal("40.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    TEST_ID, TEST_ID, new BigDecimal("100.00"), List.of(), "Test Note 2")));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Total amount does not match sum of items..."));

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            TEST_ID,
            new BigDecimal("100.00"),
            "",
            List.of(
                new TransactionItemRequest(
                    TEST_ID, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    TEST_ID,
                    UUID.randomUUID(),
                    new BigDecimal("50.00"),
                    List.of(),
                    "Test Note 2")));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category does not exist..."));

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

    UUID ctId1 = UUID.randomUUID();
    UUID ctId2 = UUID.randomUUID();
    UUID cId1 = UUID.randomUUID();
    UUID cId2 = UUID.randomUUID();
    UUID cId3 = UUID.randomUUID();

    testDataHelper.insertCategoryType(ctId1, "INCOME");
    testDataHelper.insertCategoryType(ctId2, "CT TWO");

    testDataHelper.insertCategory(cId1, ctId1, "INCOME C");
    testDataHelper.insertCategory(cId2, ctId2, "C TWO");
    testDataHelper.insertCategory(cId3, ctId2, "C THREE");

    req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Some Merchant",
            TEST_ID,
            new BigDecimal("150.00"),
            "",
            List.of(
                new TransactionItemRequest(
                    null, cId1, new BigDecimal("50.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    null, cId2, new BigDecimal("50.00"), List.of(), "Test Note 2"),
                new TransactionItemRequest(
                    null, cId3, new BigDecimal("50.00"), List.of(), "Test Note 3")));
    resp = httpPost(ApiPaths.TRANSACTIONS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(
        resp.body()
            .contains("Category type [INCOME] cannot be mixed with other category types..."));
  }

  @Test
  void testTransactionsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();
    TransactionRequest req =
        new TransactionRequest(
            LocalDateTime.now(),
            "Test Merchant",
            TEST_ID,
            new BigDecimal("100.00"),
            "",
            List.of(
                new TransactionItemRequest(
                    randomId, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 1"),
                new TransactionItemRequest(
                    randomId, TEST_ID, new BigDecimal("50.00"), List.of(), "Test Note 2")));
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
