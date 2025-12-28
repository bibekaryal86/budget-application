package budget.application.server.utils;

import budget.application.common.Exceptions;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
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
import io.netty.util.CharsetUtil;
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
      log.error("Error parsing request body: [{}]", e.getMessage());
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
}
