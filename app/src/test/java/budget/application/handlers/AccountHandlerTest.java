package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import budget.application.service.util.ResponseMetadataUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountHandlerTest extends IntegrationBaseTest {

  @Test
  void testAccounts() throws Exception {
    // CREATE
    AccountRequest req =
        new AccountRequest("Name Test", "CASH", "Bank Test", "ACTIVE");
    HttpResponse<String> resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(201, resp.statusCode());
    AccountResponse response = JsonUtils.fromJson(resp.body(), AccountResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
    Assertions.assertEquals(new BigDecimal("0.00"), response.data().getFirst().accountBalance());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultInsertResponseMetadata(), response.metadata());
    final String id = response.data().getFirst().id().toString();

    // READ ALL
    resp = httpGet(ApiPaths.ACCOUNTS_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), AccountResponse.class);
    Assertions.assertEquals(2, response.data().size());

    // READ ONE
    resp = httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), AccountResponse.class);
    Assertions.assertEquals(1, response.data().size());

    // UPDATE
    req =
        new AccountRequest(
            "Name Updated", "CASH", "Bank Updated ", "ACTIVE");
    resp = httpPut(ApiPaths.ACCOUNTS_V1_WITH_ID + id, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), AccountResponse.class);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(req.name().toUpperCase(), response.data().getFirst().name());
    Assertions.assertEquals(new BigDecimal("0.00"), response.data().getFirst().accountBalance());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultUpdateResponseMetadata(), response.metadata());

    // DELETE
    resp = httpDelete(ApiPaths.ACCOUNTS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), AccountResponse.class);
    Assertions.assertEquals(0, response.data().size());
    Assertions.assertEquals(
        ResponseMetadataUtils.defaultDeleteResponseMetadata(1), response.metadata());

    resp = httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + id, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    ResponseWithMetadata notFoundResp = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertTrue(
        notFoundResp
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("[Account] Not found for"));
  }

  @Test
  void testAccountsUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.ACCOUNTS_V1, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.ACCOUNTS_V1, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpPut(ApiPaths.ACCOUNTS_V1_WITH_ID + "some-id", "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpDelete(ApiPaths.ACCOUNTS_V1_WITH_ID + "some-id", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }

  @Test
  void testAccountsBadRequest() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.ACCOUNTS_V1, "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Account request cannot be null..."));

    AccountRequest req = new AccountRequest(null, null, "", "");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Account name cannot be empty..."));

    req = new AccountRequest("Some name", "", "", "");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Account type cannot be empty..."));

    req = new AccountRequest("Some name", "CREDIT", "", "");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Bank name cannot be empty..."));

    req = new AccountRequest("Some name", "CREDIT", "Some bank", "");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Account status cannot be empty..."));

    req =
        new AccountRequest("Some name", "CREDIT", "Some bank", "SOMETHING");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Account status is invalid..."));

    req =
        new AccountRequest("TEST ACCOUNT", "CREDIT", "Some bank", "ACTIVE");
    resp = httpPost(ApiPaths.ACCOUNTS_V1, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("(TEST ACCOUNT) already exists."));

    resp = httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp =
        httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + UUID.randomUUID() + "/something-else", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpPut(ApiPaths.ACCOUNTS_V1_WITH_ID + "invalid-uuid", "", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));

    resp = httpDelete(ApiPaths.ACCOUNTS_V1_WITH_ID + "invalid-uuid", Boolean.TRUE);
    Assertions.assertEquals(400, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("Invalid Id Provided..."));
  }

  @Test
  void testAccountsNotFound() throws Exception {
    UUID randomId = UUID.randomUUID();
    AccountRequest req =
        new AccountRequest("Some name", "CREDIT", "Some bank", "ACTIVE");
    HttpResponse<String> resp =
        httpPut(ApiPaths.ACCOUNTS_V1_WITH_ID + randomId, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Account] Not found for [" + randomId + "]"));

    resp = httpGet(ApiPaths.ACCOUNTS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Account] Not found for [" + randomId + "]"));

    resp = httpDelete(ApiPaths.ACCOUNTS_V1_WITH_ID + randomId, Boolean.TRUE);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("[Account] Not found for [" + randomId + "]"));
  }
}
