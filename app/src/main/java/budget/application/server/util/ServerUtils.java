package budget.application.server.util;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.RequestParams;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtils {
  private static final Logger log = LoggerFactory.getLogger(ServerUtils.class);

  private ServerUtils() {}

  public static void sendResponse(
      ChannelHandlerContext channelHandlerContext,
      HttpResponseStatus httpResponseStatus,
      Object bodyObject) {
    String jsonBody = JsonUtils.toJson(bodyObject);
    FullHttpResponse fullHttpResponse =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            httpResponseStatus,
            Unpooled.copiedBuffer(jsonBody, CharsetUtil.UTF_8));
    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, jsonBody.length());
    channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
  }

  public static UUID getEntityId(String path, String prefix) {
    try {
      String id = path.substring((prefix).length());
      return UUID.fromString(id);
    } catch (Exception e) {
      throw new Exceptions.BadRequestException("Invalid Id Provided...");
    }
  }

  public static <T> T getRequestBody(FullHttpRequest fullHttpRequest, Class<T> type) {
    try {
      return JsonUtils.fromJson(fullHttpRequest.content().toString(CharsetUtil.UTF_8), type);
    } catch (Exception ex) {
      log.error("Error parsing request bodyObject", ex);
      return null;
    }
  }

  public static ResponseWithMetadata getResponseWithMetadata(String errMsg) {
    return new ResponseWithMetadata(
        new ResponseMetadata(
            new ResponseMetadata.ResponseStatusInfo(errMsg),
            ResponseMetadata.emptyResponseCrudInfo(),
            ResponseMetadata.emptyResponsePageInfo()));
  }

  public static RequestParams.CategoryParams getCategoryParams(QueryStringDecoder decoder) {
    return new RequestParams.CategoryParams(parseUUIDs(decoder, "categoryTypeIds"));
  }

  public static PaginationRequest getPaginationRequest(QueryStringDecoder decoder) {
    int pageNumber = parseIntNoEx(decoder, "pageNumber");
    if (pageNumber == 0) {
      pageNumber = Constants.DEFAULT_PAGE_NUMBER;
    }
    int perPage = parseIntNoEx(decoder, "perPage");
    if (perPage == 0) {
      perPage = Constants.DEFAULT_PER_PAGE;
    }
    return new PaginationRequest(pageNumber, perPage);
  }

  public static RequestParams.TransactionParams getTransactionParams(QueryStringDecoder decoder) {
    LocalDate beginDate = parseDate(decoder, "beginDate");
    LocalDate endDate = parseDate(decoder, "endDate");
    List<String> merchants = parseStrings(decoder, "merchants");
    List<UUID> categoryIds = parseUUIDs(decoder, "categoryIds");
    List<UUID> categoryTypeIds = parseUUIDs(decoder, "categoryTypeIds");
    List<UUID> accountIds = parseUUIDs(decoder, "accountIds");
    List<String> tags = parseStrings(decoder, "tags");
    return new RequestParams.TransactionParams(
        beginDate, endDate, merchants, categoryIds, categoryTypeIds, accountIds, tags);
  }

  public static RequestParams.BudgetParams getBudgetParams(QueryStringDecoder decoder) {
    int budgetMonth = parseInt(decoder, "budgetMonth");
    int budgetYear = parseInt(decoder, "budgetYear");
    List<UUID> categoryIds = parseUUIDs(decoder, "categoryIds");
    return new RequestParams.BudgetParams(budgetMonth, budgetYear, categoryIds);
  }

  public static RequestParams.CashFlowSummaryParams getCashFlowSummaryParams(
      QueryStringDecoder decoder) {
    LocalDate beginDate = parseDate(decoder, "beginDate");
    LocalDate endDate = parseDate(decoder, "endDate");
    int monthsAgo = parseInt(decoder, "monthsAgo");

    if (monthsAgo == 0) {
      monthsAgo = 2;
    }

    if (beginDate == null && endDate == null) {
      LocalDate now = LocalDate.now();
      beginDate = now.withDayOfMonth(1);
      endDate = now.withDayOfMonth(now.lengthOfMonth());
      return new RequestParams.CashFlowSummaryParams(beginDate, endDate, monthsAgo);
    }

    if (beginDate != null && endDate == null) {
      endDate = beginDate.withDayOfMonth(beginDate.lengthOfMonth());
      return new RequestParams.CashFlowSummaryParams(beginDate, endDate, monthsAgo);
    }

    if (beginDate == null && endDate != null) {
      beginDate = endDate.withDayOfMonth(1);
      return new RequestParams.CashFlowSummaryParams(beginDate, endDate, monthsAgo);
    }

    return new RequestParams.CashFlowSummaryParams(beginDate, endDate, monthsAgo);
  }

  public static RequestParams.CategorySummaryParams getCategorySummaryParams(
      QueryStringDecoder decoder) {

    LocalDate beginDate = parseDate(decoder, "beginDate");
    LocalDate endDate = parseDate(decoder, "endDate");

    if (beginDate == null && endDate == null) {
      LocalDate now = LocalDate.now();
      beginDate = now.withDayOfMonth(1);
      endDate = now.withDayOfMonth(now.lengthOfMonth());
    } else if (beginDate != null && endDate == null) {
      endDate = beginDate.withDayOfMonth(beginDate.lengthOfMonth());
    } else if (beginDate == null && endDate != null) {
      beginDate = endDate.withDayOfMonth(1);
    }

    List<UUID> categoryIds = parseUUIDs(decoder, "categoryIds");
    List<UUID> categoryTypeIds = parseUUIDs(decoder, "categoryTypeIds");
    int topExpenses = parseInt(decoder, "topExpenses");
    int monthsAgo = parseInt(decoder, "monthsAgo");

    return new RequestParams.CategorySummaryParams(
        beginDate, endDate, categoryIds, categoryTypeIds, topExpenses, monthsAgo);
  }

  private static List<String> getParameterValues(QueryStringDecoder decoder, String paramName) {
    List<String> values = new ArrayList<>();

    // Check for both formats: "param" and "param[]"
    List<String> standardValues = decoder.parameters().get(paramName);
    List<String> arrayValues = decoder.parameters().get(paramName + "[]");

    if (!CommonUtilities.isEmpty(standardValues)) {
      values.addAll(standardValues);
    }

    if (!CommonUtilities.isEmpty(arrayValues)) {
      values.addAll(arrayValues);
    }

    return values;
  }

  private static LocalDate parseDate(QueryStringDecoder decoder, String paramName) {
    List<String> values = decoder.parameters().get(paramName);
    if (CommonUtilities.isEmpty(values)) {
      return null;
    }
    return LocalDate.parse(values.getFirst());
  }

  private static List<UUID> parseUUIDs(QueryStringDecoder decoder, String paramName) {
    List<String> values = getParameterValues(decoder, paramName);

    if (CommonUtilities.isEmpty(values)) {
      return List.of();
    }

    return values.stream()
        .flatMap(v -> Arrays.stream(v.split(",")))
        .filter(s -> !CommonUtilities.isEmpty(s))
        .map(String::trim)
        .map(UUID::fromString)
        .toList();
  }

  private static List<String> parseStrings(QueryStringDecoder decoder, String paramName) {
    List<String> values = getParameterValues(decoder, paramName);

    if (CommonUtilities.isEmpty(values)) {
      return List.of();
    }

    return values.stream()
        .flatMap(v -> Arrays.stream(v.split(",")))
        .filter(s -> !CommonUtilities.isEmpty(s))
        .map(String::trim)
        .toList();
  }

  private static int parseInt(QueryStringDecoder decoder, String paramName) {
    List<String> values = getParameterValues(decoder, paramName);

    if (CommonUtilities.isEmpty(values)) {
      return 0;
    }

    return values.stream()
        .flatMap(v -> Arrays.stream(v.split(",")))
        .filter(s -> !CommonUtilities.isEmpty(s))
        .map(String::trim)
        .findFirst()
        .map(Integer::parseInt)
        .orElse(0);
  }

  private static int parseIntNoEx(QueryStringDecoder decoder, String paramName) {
    try {
      return parseInt(decoder, paramName);
    } catch (Exception e) {
      return 0;
    }
  }

  private static boolean parseBoolean(QueryStringDecoder decoder, String paramName) {
    List<String> values = getParameterValues(decoder, paramName);

    if (CommonUtilities.isEmpty(values)) {
      return false;
    }

    return values.stream()
        .flatMap(v -> Arrays.stream(v.split(",")))
        .filter(s -> !CommonUtilities.isEmpty(s))
        .map(String::trim)
        .findFirst()
        .map(Boolean::parseBoolean)
        .orElse(false);
  }
}
