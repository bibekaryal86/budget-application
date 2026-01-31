package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.TransactionResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BudgetHandlerTest extends IntegrationBaseTest {

  @AfterEach
  void cleanup() throws SQLException {
    testDataHelper.deleteBudget(List.of(TEST_ID));
  }

  @Test
  void testBudgets() throws Exception {
    // CREATE
    BudgetRequest req =
        new BudgetRequest(
            TEST_ID,
            LocalDate.now().getMonth().getValue(),
            LocalDate.now().getYear(),
            new BigDecimal("100.00"),
            "");
    HttpResponse<String> resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    BudgetResponse response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.categoryId(), response.data().getFirst().category().id());
    Assertions.assertEquals(req.amount(), response.data().getFirst().amount());
    Assertions.assertTrue(CommonUtilities.isEmpty(response.data().getFirst().notes()));
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.BUDGETS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req =
        new BudgetRequest(
            TEST_ID,
            LocalDate.now().getMonth().getValue(),
            LocalDate.now().getYear(),
            new BigDecimal("200.00"),
            "some notes");
    resp = httpPut(ApiPaths.BUDGETS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.categoryId(), response.data().getFirst().category().id());
    Assertions.assertEquals(req.amount(), response.data().getFirst().amount());
    Assertions.assertFalse(CommonUtilities.isEmpty(response.data().getFirst().notes()));
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.BUDGETS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[Budget] Not found for"));
  }

  @Test
  void testBudgetsUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.BUDGETS_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.BUDGETS_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.BUDGETS_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.BUDGETS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testBudgetsBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.BUDGETS_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget request cannot be null..."));

    BudgetRequest req =
        new BudgetRequest(
            null,
            LocalDate.now().getMonth().getValue(),
            LocalDate.now().getYear(),
            new BigDecimal("100.00"),
            "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget category cannot be null..."));

    req = new BudgetRequest(TEST_ID, 0, LocalDate.now().getYear(), new BigDecimal("100.00"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget month should be between 1 and 12..."));

    req = new BudgetRequest(TEST_ID, 13, LocalDate.now().getYear(), new BigDecimal("100.00"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget month should be between 1 and 12..."));

    req = new BudgetRequest(TEST_ID, 7, 2020, new BigDecimal("100.00"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget year should be between 2025 and 2100..."));

    req = new BudgetRequest(TEST_ID, 7, 2101, new BigDecimal("100.00"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget year should be between 2025 and 2100..."));

    req = new BudgetRequest(TEST_ID, 7, 2026, null, "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget amount cannot be zero or negative..."));

    req = new BudgetRequest(TEST_ID, 7, 2026, new BigDecimal("0.0"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget amount cannot be zero or negative..."));

    req = new BudgetRequest(UUID.randomUUID(), 7, 2026, new BigDecimal("10.0"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Category does not exist..."));

    // unique constraint test
    // step1: setup
    req = new BudgetRequest(TEST_ID, 7, 2026, new BigDecimal("10.0"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    // step2: check
    req = new BudgetRequest(TEST_ID, 7, 2026, new BigDecimal("10.0"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("(" + TEST_ID + ", 7, 2026) already exists."));

    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(ApiPaths.BUDGETS_V1_WITH_ID + UUID.randomUUID() + "/something-else", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.BUDGETS_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.BUDGETS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testBudgetsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();
    BudgetRequest req = new BudgetRequest(TEST_ID, 7, 2026, new BigDecimal("10.0"), "");
    HttpResponse<String> resp =
        httpPut(ApiPaths.BUDGETS_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Budget] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Budget] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.BUDGETS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Budget] Not found for [" + randomId + "]"));
  }

  @Test
  void testReadBudgets() throws Exception {
    // SETUP
    UUID cId1 = UUID.randomUUID();
    UUID cId2 = UUID.randomUUID();
    UUID cId3 = UUID.randomUUID();

    UUID bId1 = UUID.randomUUID();
    UUID bId2 = UUID.randomUUID();
    UUID bId3 = UUID.randomUUID();
    UUID bId4 = UUID.randomUUID();

    testDataHelper.insertCategory(cId1, TEST_ID, "C ONE");
    testDataHelper.insertCategory(cId2, TEST_ID, "C TWO");
    testDataHelper.insertCategory(cId3, TEST_ID, "C THREE");

    testDataHelper.insertBudget(bId1, cId1, 7, 2025);
    testDataHelper.insertBudget(bId4, cId1, 8, 2026);
    testDataHelper.insertBudget(bId2, cId2, 7, 2025);
    testDataHelper.insertBudget(bId3, cId3, 7, 2025);

    HttpResponse<String> resp = httpGet(ApiPaths.BUDGETS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    TransactionResponse response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(4, response.data().size());

    resp =
        httpGet(
            ApiPaths.BUDGETS_V1 + "?categoryIds=" + TEST_ID + "," + cId2 + "," + cId3,
            Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // returns all 4 because year is not provided
    resp = httpGet(ApiPaths.BUDGETS_V1 + "?budgetMonth=7", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(4, response.data().size());

    // returns all 4 because month is not provided
    resp = httpGet(ApiPaths.BUDGETS_V1 + "?budgetYear=2025", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(4, response.data().size());

    resp = httpGet(ApiPaths.BUDGETS_V1 + "?budgetMonth=7&budgetYear=2025", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(3, response.data().size());

    resp =
        httpGet(
            ApiPaths.BUDGETS_V1
                + "?categoryIds="
                + TEST_ID
                + ","
                + cId1
                + "&budgetMonth=7"
                + "&budgetYear=2025",
            Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), TransactionResponse.class);
    Assertions.assertEquals(1, response.data().size());
  }
}
