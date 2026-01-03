package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppTestsHandlerTest extends IntegrationBaseTest {

  @Test
  void testAppTestPing() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.APP_TESTS_PING, Boolean.FALSE);
    Assertions.assertEquals(200, resp.statusCode());
    Assertions.assertTrue(resp.body().contains("ping") && resp.body().contains("successful"));
  }

  @Test
  void testUnknownTestsPathFallsThrough() throws Exception {
    HttpResponse<String> resp = httpGet("/tests/unknown", Boolean.TRUE);
    ResponseWithMetadata response = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(
        response
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("The requested resource does not exist..."));
  }

  @Test
  void testUnknownPathFallsThrough() throws Exception {
    HttpResponse<String> resp = httpGet("/petssvc/api/v1/unknown", Boolean.TRUE);
    ResponseWithMetadata response = JsonUtils.fromJson(resp.body(), ResponseWithMetadata.class);
    Assertions.assertEquals(404, resp.statusCode());
    Assertions.assertTrue(
        response
            .getResponseMetadata()
            .responseStatusInfo()
            .errMsg()
            .contains("The requested resource does not exist..."));
  }

  @Test
  void testPingIsHandledOnlyByAppTestsHandler() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.APP_TESTS_PING, Boolean.FALSE);
    String body = resp.body();
    Assertions.assertTrue(body.contains("ping"));
    Assertions.assertFalse(body.contains("data"));
  }

  @Test
  void testNonMatchingPathFallsThrough() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.CATEGORY_TYPES_V1, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    String body = resp.body();
    Assertions.assertFalse(body.contains("ping"));
  }
}
