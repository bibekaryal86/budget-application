package budget.application.server.util;

import budget.application.common.Exceptions;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtils {
  private static final Logger log = LoggerFactory.getLogger(ServerUtils.class);

  private ServerUtils() {}

  public static void sendResponse(
      ChannelHandlerContext ctx, HttpResponseStatus status, Object body) {
    String jsonBody = JsonUtils.toJson(body);
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(jsonBody, CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, jsonBody.length());
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  public static UUID getEntityId(String path, String prefix) {
    try {
      String id = path.substring((prefix).length());
      return UUID.fromString(id);
    } catch (Exception e) {
      throw new Exceptions.BadRequestException("Invalid Id Provided...");
    }
  }

  public static <T> T getRequestBody(FullHttpRequest req, Class<T> type) {
    try {
      return JsonUtils.fromJson(req.content().toString(CharsetUtil.UTF_8), type);
    } catch (Exception e) {
      log.error("Error parsing request body", e);
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
    return new RequestParams.CategoryParams(parseUUIDs(decoder, "catTypeIds"));
  }

  public static RequestParams.TransactionParams getTransactionParams(QueryStringDecoder decoder) {
    LocalDate beginDate = parseDate(decoder, "beginDate");
    LocalDate endDate = parseDate(decoder, "endDate");
    List<String> merchants = parseStrings(decoder, "merchants");
    List<UUID> catIds = parseUUIDs(decoder, "catIds");
    List<UUID> catTypeIds = parseUUIDs(decoder, "catTypeIds");
    List<UUID> accIds = parseUUIDs(decoder, "accIds");
    List<String> expTypes = parseStrings(decoder, "expTypes");
    if (!expTypes.isEmpty()) {
      log.debug("expTypes: {}", expTypes);
    }
    return new RequestParams.TransactionParams(
        beginDate, endDate, merchants, catIds, catTypeIds, accIds, expTypes);
  }

  private static List<UUID> parseUUIDs(QueryStringDecoder decoder, String paramName) {
    List<String> values = decoder.parameters().get(paramName);
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

  private static LocalDate parseDate(QueryStringDecoder decoder, String paramName) {
    List<String> values = decoder.parameters().get(paramName);
    if (CommonUtilities.isEmpty(values)) {
      return null;
    }
    return LocalDate.parse(values.getFirst());
  }

  private static List<String> parseStrings(QueryStringDecoder decoder, String paramName) {
    List<String> values = decoder.parameters().get(paramName);
    if (CommonUtilities.isEmpty(values)) {
      return List.of();
    }
    return values.stream()
        .flatMap(v -> Arrays.stream(v.split(",")))
        .filter(s -> !CommonUtilities.isEmpty(s))
        .map(String::trim)
        .toList();
  }
}
