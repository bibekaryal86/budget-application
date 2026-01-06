package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;

import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BudgetHandlerTest extends IntegrationBaseTest {

  @Test
  void testBudgets() throws Exception {
    // CREATE
    BudgetRequest req = new BudgetRequest(TEST_ID, LocalDate.now().getMonth().getValue(), LocalDate.now().getYear(), new BigDecimal("100.00"), "");
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
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.BUDGETS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), BudgetResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req = new BudgetRequest(TEST_ID, LocalDate.now().getMonth().getValue(), LocalDate.now().getYear(), new BigDecimal("200.00"), "some notes");
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

    BudgetRequest req = new BudgetRequest(null, null, "", null, "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget name cannot be empty..."));

    req = new BudgetRequest("Some name", "", "", null, "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget type cannot be empty..."));

    req = new BudgetRequest("Some name", "CREDIT", "", null, "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Bank name cannot be empty..."));

    req = new BudgetRequest("Some name", "CREDIT", "Some bank", null, "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Opening balance cannot be null or negative..."));

    req = new BudgetRequest("Some name", "CREDIT", "Some bank", new BigDecimal("-1.0"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Opening balance cannot be null or negative..."));

    req = new BudgetRequest("Some name", "CREDIT", "Some bank", new BigDecimal("0.00"), "");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget status cannot be empty..."));

    req =
        new BudgetRequest("Some name", "CREDIT", "Some bank", new BigDecimal("0.00"), "SOMETHING");
    resp = httpPost(ApiPaths.BUDGETS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Budget status is invalid..."));

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
    BudgetRequest req =
        new BudgetRequest("Some name", "CREDIT", "Some bank", new BigDecimal("0.00"), "ACTIVE");
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
}
