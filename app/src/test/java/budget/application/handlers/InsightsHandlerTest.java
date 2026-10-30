package budget.application.handlers;

import budget.application.IntegrationBaseTest;
import budget.application.common.Constants;
import budget.application.db.util.DaoUtils;
import budget.application.model.dto.AccountResponse;
import budget.application.model.dto.CategoryResponse;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.model.dto.InsightsResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.JsonUtils;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InsightsHandlerTest extends IntegrationBaseTest {
  UUID ctIncome;
  UUID ctSaving;
  UUID ctTransfer;
  UUID ctExpense;
  UUID cIncomeOne;
  UUID cIncomeTwo;
  UUID cSavingOne;
  UUID cSavingTwo;
  UUID cTransferOne;
  UUID cTransferTwo;
  UUID cExpenseOne;
  UUID cExpenseTwo;
  UUID accIdOne;

  @BeforeEach
  void setup() throws SQLException {
    ctIncome = UUID.randomUUID();
    ctSaving = UUID.randomUUID();
    ctTransfer = UUID.randomUUID();
    ctExpense = UUID.randomUUID();
    cIncomeOne = UUID.randomUUID();
    cIncomeTwo = UUID.randomUUID();
    cSavingOne = UUID.randomUUID();
    cSavingTwo = UUID.randomUUID();
    cTransferOne = UUID.randomUUID();
    cTransferTwo = UUID.randomUUID();
    cExpenseOne = UUID.randomUUID();
    cExpenseTwo = UUID.randomUUID();
    accIdOne = UUID.randomUUID();

    testDataHelper.insertCategoryType(ctIncome, Constants.CATEGORY_TYPE_INCOME_NAME);
    testDataHelper.insertCategoryType(ctSaving, Constants.CATEGORY_TYPE_SAVINGS_NAME);
    testDataHelper.insertCategoryType(ctTransfer, Constants.CATEGORY_TYPE_TRANSFER_NAME);
    testDataHelper.insertCategoryType(ctExpense, "EXPENSE");

    testDataHelper.insertCategory(cIncomeOne, ctIncome, "INCOME ONE");
    testDataHelper.insertCategory(cIncomeTwo, ctIncome, "INCOME TWO");
    testDataHelper.insertCategory(cSavingOne, ctSaving, "SAVINGS ONE");
    testDataHelper.insertCategory(cSavingTwo, ctSaving, "SAVINGS TWO");
    testDataHelper.insertCategory(cTransferOne, ctTransfer, Constants.CATEGORY_TRANSFER_IN);
    testDataHelper.insertCategory(cTransferTwo, ctTransfer, Constants.CATEGORY_TRANSFER_OUT);
    testDataHelper.insertCategory(cExpenseOne, ctExpense, "EXPENSE ONE");
    testDataHelper.insertCategory(cExpenseTwo, ctExpense, "EXPENSE TWO");

    testDataHelper.insertTransaction(cIncomeOne, LocalDateTime.now(), 100.00);
    testDataHelper.insertTransaction(cIncomeTwo, LocalDateTime.now().minusMonths(1), 500.00);
    testDataHelper.insertTransaction(cSavingOne, LocalDateTime.now(), 100.00);
    testDataHelper.insertTransaction(cSavingTwo, LocalDateTime.now().minusMonths(1), 500.00);
    testDataHelper.insertTransaction(cTransferOne, LocalDateTime.now(), 1000.00);
    testDataHelper.insertTransaction(cExpenseOne, LocalDateTime.now(), 700.00);
    testDataHelper.insertTransaction(cExpenseTwo, LocalDateTime.now().minusMonths(1), 800.00);

    testDataHelper.insertTransactionItem(cIncomeOne, cIncomeOne, cIncomeOne, 100.00, List.of());
    testDataHelper.insertTransactionItem(cIncomeTwo, cIncomeTwo, cIncomeTwo, 500.00, List.of());
    testDataHelper.insertTransactionItem(cSavingOne, cSavingOne, cSavingOne, 100.00, List.of());
    testDataHelper.insertTransactionItem(cSavingTwo, cSavingTwo, cSavingOne, 500.00, List.of());
    testDataHelper.insertTransactionItem(
        cTransferOne, cTransferOne, cTransferOne, 500.00, List.of());
    testDataHelper.insertTransactionItem(
        cTransferTwo, cTransferOne, cTransferTwo, 500.00, List.of());
    testDataHelper.insertTransactionItem(cExpenseOne, cExpenseOne, cExpenseOne, 700.00, List.of());
    testDataHelper.insertTransactionItem(cExpenseTwo, cExpenseTwo, cExpenseTwo, 800.00, List.of());

    testDataHelper.insertAccount(accIdOne, "CREDIT", "TEST ACCOUNT CREDIT");
    testDataHelper.insertAccountBalance(
        cIncomeOne, accIdOne, LocalDate.now().minusMonths(1), "100.00");
    testDataHelper.insertAccountBalance(cIncomeTwo, accIdOne, LocalDate.now(), "1000.00");
  }

  @AfterEach
  void cleanup() throws SQLException {
    testDataHelper.deleteTransactionItem(List.of(TEST_ID));
    testDataHelper.deleteTransaction(List.of(TEST_ID));
    testDataHelper.deleteCategory(List.of(TEST_ID));
    testDataHelper.deleteCategoryType(List.of(TEST_ID));
    testDataHelper.deleteAccountBalance(List.of(TEST_ID));
    testDataHelper.deleteAccount(List.of(TEST_ID));
  }

  @Test
  void testInsightsCashFlowSummary() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.INSIGHTS_V1_CF_SUMMARIES, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    InsightsResponse.CashFlowSummaries response =
        JsonUtils.fromJson(resp.body(), InsightsResponse.CashFlowSummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(1, response.data().size());

    resp = httpGet(ApiPaths.INSIGHTS_V1_CF_SUMMARIES + "?totalMonths=2", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.CashFlowSummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.data().size());
    Assertions.assertEquals(
        List.of(
            new InsightsResponse.CashFlowSummary(
                DaoUtils.getYearMonth(LocalDate.now().minusMonths(1)),
                new InsightsResponse.CashFlowAmounts(
                    new BigDecimal("500.00"),
                    new BigDecimal("800.00"),
                    new BigDecimal("500.00"),
                    new BigDecimal("-800.00"))),
            new InsightsResponse.CashFlowSummary(
                DaoUtils.getYearMonth(LocalDate.now()),
                new InsightsResponse.CashFlowAmounts(
                    new BigDecimal("100.00"),
                    new BigDecimal("801.01"),
                    new BigDecimal("100.00"),
                    new BigDecimal("-801.01")))),
        response.data());
  }

  @Test
  void testInsightsCategorySummary() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    InsightsResponse.CategorySummaries response =
        JsonUtils.fromJson(resp.body(), InsightsResponse.CategorySummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(9, response.data().getFirst().categoryAmounts().size());

    resp =
        httpGet(
            ApiPaths.INSIGHTS_V1_CAT_SUMMARIES + "?categoryTypeIds=" + ctExpense + "," + ctIncome,
            Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.CategorySummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(4, response.data().getFirst().categoryAmounts().size());

    resp =
        httpGet(
            ApiPaths.INSIGHTS_V1_CAT_SUMMARIES
                + "?categoryTypeIds="
                + ctExpense
                + ","
                + ctIncome
                + "&categoryIds="
                + cExpenseOne
                + ","
                + cIncomeOne,
            Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.CategorySummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(1, response.data().size());
    Assertions.assertEquals(2, response.data().getFirst().categoryAmounts().size());

    resp = httpGet(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES + "?totalMonths=2", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.CategorySummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.data().size());
    Assertions.assertEquals(
        List.of(
            new InsightsResponse.CategorySummary(
                DaoUtils.getYearMonth(LocalDate.now()),
                List.of(
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cExpenseOne,
                            new CategoryTypeResponse.CategoryType(ctExpense, "EXPENSE"),
                            "EXPENSE ONE"),
                        new BigDecimal("700.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cExpenseTwo,
                            new CategoryTypeResponse.CategoryType(ctExpense, "EXPENSE"),
                            "EXPENSE TWO"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cIncomeOne,
                            new CategoryTypeResponse.CategoryType(ctIncome, "INCOME"),
                            "INCOME ONE"),
                        new BigDecimal("100.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cIncomeTwo,
                            new CategoryTypeResponse.CategoryType(ctIncome, "INCOME"),
                            "INCOME TWO"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cSavingOne,
                            new CategoryTypeResponse.CategoryType(ctSaving, "SAVINGS"),
                            "SAVINGS ONE"),
                        new BigDecimal("100.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cSavingTwo,
                            new CategoryTypeResponse.CategoryType(ctSaving, "SAVINGS"),
                            "SAVINGS TWO"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            TEST_ID,
                            new CategoryTypeResponse.CategoryType(TEST_ID, "TEST CATEGORY TYPE"),
                            "TEST CATEGORY"),
                        new BigDecimal("101.01")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cTransferOne,
                            new CategoryTypeResponse.CategoryType(ctTransfer, "TRANSFER"),
                            "TRANSFER IN"),
                        new BigDecimal("500.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cTransferTwo,
                            new CategoryTypeResponse.CategoryType(ctTransfer, "TRANSFER"),
                            "TRANSFER OUT"),
                        new BigDecimal("500.00")))),
            new InsightsResponse.CategorySummary(
                DaoUtils.getYearMonth(LocalDate.now().minusMonths(1)),
                List.of(
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cExpenseOne,
                            new CategoryTypeResponse.CategoryType(ctExpense, "EXPENSE"),
                            "EXPENSE ONE"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cExpenseTwo,
                            new CategoryTypeResponse.CategoryType(ctExpense, "EXPENSE"),
                            "EXPENSE TWO"),
                        new BigDecimal("800.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cIncomeOne,
                            new CategoryTypeResponse.CategoryType(ctIncome, "INCOME"),
                            "INCOME ONE"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cIncomeTwo,
                            new CategoryTypeResponse.CategoryType(ctIncome, "INCOME"),
                            "INCOME TWO"),
                        new BigDecimal("500.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cSavingOne,
                            new CategoryTypeResponse.CategoryType(ctSaving, "SAVINGS"),
                            "SAVINGS ONE"),
                        new BigDecimal("500.00")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cSavingTwo,
                            new CategoryTypeResponse.CategoryType(ctSaving, "SAVINGS"),
                            "SAVINGS TWO"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            TEST_ID,
                            new CategoryTypeResponse.CategoryType(TEST_ID, "TEST CATEGORY TYPE"),
                            "TEST CATEGORY"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cTransferOne,
                            new CategoryTypeResponse.CategoryType(ctTransfer, "TRANSFER"),
                            "TRANSFER IN"),
                        new BigDecimal("0")),
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cTransferTwo,
                            new CategoryTypeResponse.CategoryType(ctTransfer, "TRANSFER"),
                            "TRANSFER OUT"),
                        new BigDecimal("0"))))),
        response.data());

    // limits by number of top expenses in the parameter
    resp =
        httpGet(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES + "?totalMonths=2&topExpenses=1", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.CategorySummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.data().size());
    Assertions.assertEquals(
        List.of(
            new InsightsResponse.CategorySummary(
                DaoUtils.getYearMonth(LocalDate.now()),
                List.of(
                    new InsightsResponse.CategoryAmount(
                        new CategoryResponse.Category(
                            cExpenseOne,
                            new CategoryTypeResponse.CategoryType(ctExpense, "EXPENSE"),
                            "EXPENSE ONE"),
                        new BigDecimal("700.00")))),
            new InsightsResponse.CategorySummary(
                DaoUtils.getYearMonth(LocalDate.now().minusMonths(1)), List.of())),
        response.data());
  }

  @Test
  void testInsightsAccountSummary() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.INSIGHTS_V1_ACC_SUMMARIES, Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    InsightsResponse.AccountSummaries response =
        JsonUtils.fromJson(resp.body(), InsightsResponse.AccountSummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(1, response.data().size());

    resp = httpGet(ApiPaths.INSIGHTS_V1_ACC_SUMMARIES + "?totalMonths=1", Boolean.TRUE);
    Assertions.assertEquals(200, resp.statusCode());
    response = JsonUtils.fromJson(resp.body(), InsightsResponse.AccountSummaries.class);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.data().size());
    Assertions.assertEquals(
        List.of(
            new InsightsResponse.AccountSummary(
                DaoUtils.getYearMonth(LocalDate.now().minusMonths(1)),
                Map.of(
                    "WORTH",
                    new BigDecimal("400.00"),
                    "DEBTS",
                    new BigDecimal("100.00"),
                    "ASSETS",
                    new BigDecimal("500.00")),
                List.of(
                    new AccountResponse.Account(
                        TEST_ID,
                        "TEST ACCOUNT",
                        "SAVINGS",
                        "TEST BANK",
                        new BigDecimal("500.00"),
                        "ACTIVE",
                        List.of()),
                    new AccountResponse.Account(
                        accIdOne,
                        "TEST ACCOUNT CREDIT",
                        "CREDIT",
                        "TEST BANK",
                        new BigDecimal("100.00"),
                        "ACTIVE",
                        List.of()))),
            new InsightsResponse.AccountSummary(
                DaoUtils.getYearMonth(LocalDate.now()),
                Map.of(
                    "WORTH",
                    new BigDecimal("-400.00"),
                    "DEBTS",
                    new BigDecimal("1000.00"),
                    "ASSETS",
                    new BigDecimal("600.00")),
                List.of(
                    new AccountResponse.Account(
                        TEST_ID,
                        "TEST ACCOUNT",
                        "SAVINGS",
                        "TEST BANK",
                        new BigDecimal("600.00"),
                        "ACTIVE",
                        List.of()),
                    new AccountResponse.Account(
                        accIdOne,
                        "TEST ACCOUNT CREDIT",
                        "CREDIT",
                        "TEST BANK",
                        new BigDecimal("1000.00"),
                        "ACTIVE",
                        List.of())))),
        response.data());
  }

  @Test
  void testInsightsUnauthorized() throws Exception {
    HttpResponse<String> resp = httpGet(ApiPaths.INSIGHTS_V1_CF_SUMMARIES, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
    resp = httpGet(ApiPaths.INSIGHTS_V1_ACC_SUMMARIES, Boolean.FALSE);
    Assertions.assertEquals(401, resp.statusCode());
  }
}
