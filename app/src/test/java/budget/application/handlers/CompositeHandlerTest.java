package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.TestDataHelper;
import budget.application.TestDataSource;
import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.JsonUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CompositeHandlerTest extends IntegrationBaseTest {
  private static TestDataHelper helper;
  private static UUID ctId1;
  private static UUID ctId2;
  private static UUID cId1;
  private static UUID cId2;
  private static UUID cId3;
  private static UUID tId1;
  private static UUID tId2;
  private static UUID tId3;
  private static UUID tiId1;
  private static UUID tiId2;
  private static UUID tiId3;

  @BeforeAll
  static void setup() throws SQLException {
    helper = new TestDataHelper(TestDataSource.getDataSource());

    ctId1 = UUID.randomUUID();
    ctId2 = UUID.randomUUID();
    cId1 = UUID.randomUUID();
    cId2 = UUID.randomUUID();
    cId3 = UUID.randomUUID();
    tId1 = UUID.randomUUID();
    tId2 = UUID.randomUUID();
    tId3 = UUID.randomUUID();
    tiId1 = UUID.randomUUID();
    tiId2 = UUID.randomUUID();
    tiId3 = UUID.randomUUID();

    helper.insertCategoryType(ctId1, "CT ONE");
    helper.insertCategoryType(ctId2, "CT TWO");

    helper.insertCategory(cId1, ctId1, "C ONE");
    helper.insertCategory(cId2, ctId1, "C TWO");
    helper.insertCategory(cId3, ctId2, "C THREE");

    helper.insertTransaction(tId1, LocalDate.now(), 100.00);
    helper.insertTransaction(tId2, LocalDate.now().minusMonths(1L), 200.00);
    helper.insertTransaction(tId3, LocalDate.now().minusMonths(2L), 300.00);

    helper.insertTransactionItem(tiId1, tId1, cId1, 50);
    helper.insertTransactionItem(tiId2, tId1, cId2, 50);
    helper.insertTransactionItem(tiId3, tId2, cId2, 200);
  }

  @AfterAll
  static void cleanup() throws SQLException {
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
  void testCompositeCategories() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, "", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    CompositeResponse response = JsonUtils.fromJson(resp.body(), CompositeResponse.class);
    Assertions.assertNull(response.txns());
    Assertions.assertEquals(ResponseMetadata.emptyResponseMetadata(), response.metadata());
    Assertions.assertEquals(4, response.cats().size());

    CompositeRequest req = new CompositeRequest(null, null);
    resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CompositeResponse.class);
    Assertions.assertNull(response.txns());
    Assertions.assertEquals(ResponseMetadata.emptyResponseMetadata(), response.metadata());
    Assertions.assertEquals(4, response.cats().size());

    req = new CompositeRequest(null, new CompositeRequest.CategoryRequest(null));
    resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CompositeResponse.class);
    Assertions.assertNull(response.txns());
    Assertions.assertEquals(ResponseMetadata.emptyResponseMetadata(), response.metadata());
    Assertions.assertEquals(4, response.cats().size());

    req = new CompositeRequest(null, new CompositeRequest.CategoryRequest(ctId1));
    resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CompositeResponse.class);
    Assertions.assertNull(response.txns());
    Assertions.assertEquals(ResponseMetadata.emptyResponseMetadata(), response.metadata());
    Assertions.assertEquals(2, response.cats().size());

    req = new CompositeRequest(null, new CompositeRequest.CategoryRequest(ctId2));
    resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), CompositeResponse.class);
    Assertions.assertNull(response.txns());
    Assertions.assertEquals(ResponseMetadata.emptyResponseMetadata(), response.metadata());
    Assertions.assertEquals(1, response.cats().size());
  }

  @Test
  void testCompositeTransactions() throws Exception {
    CompositeRequest req = new CompositeRequest(null, null);
    HttpResponse<String> resp =
        httpPost(ApiPaths.COMPOSITE_V1_TRANSACTIONS, JsonUtils.toJson(req), Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
  }

  @Test
  void testCategoriesUnauthorized() throws Exception {
    HttpResponse<String> resp = httpPost(ApiPaths.COMPOSITE_V1_CATEGORIES, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());

    resp = httpPost(ApiPaths.COMPOSITE_V1_TRANSACTIONS, "", Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }
}
