package budget.application.server.core;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.server.util.ServerUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import java.sql.SQLException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ServerLogging extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(ServerLogging.class);

  @Override
  public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object object)
      throws Exception {
    if (object instanceof FullHttpRequest fullHttpRequest) {
      MDC.put("requestId", UUID.randomUUID().toString());

      final String requestContentLength =
          fullHttpRequest
              .headers()
              .get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
      log.info(
          "Request IN: Method=[{}], Uri=[{}], ContentLength=[{}]",
          fullHttpRequest.method(),
          fullHttpRequest.uri(),
          requestContentLength);
    }
    super.channelRead(channelHandlerContext, object);
  }

  @Override
  public void write(
      final ChannelHandlerContext channelHandlerContext,
      final Object object,
      final ChannelPromise channelPromise)
      throws Exception {
    try {
      if (object instanceof FullHttpResponse fullHttpResponse) {
        final String responseContentLength =
            fullHttpResponse
                .headers()
                .get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
        final HttpResponseStatus responseStatus = fullHttpResponse.status();

        log.info(
            "Response OUT: Status=[{}], ContentLength=[{}]", responseStatus, responseContentLength);
      }
    } finally {
      MDC.clear();
    }
    super.write(channelHandlerContext, object, channelPromise);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    try {
      final String className = cause.getClass().getSimpleName();
      String message = cause.getMessage();

      log.error("Exception caught...", cause);
      ResponseWithMetadata response =
          ServerUtils.getResponseWithMetadata(String.format("[%s]--[%s]", className, message));
      ServerUtils.sendResponse(ctx, getHttpStatus(cause), response);
    } finally {
      MDC.clear();
    }
  }

  private HttpResponseStatus getHttpStatus(Throwable cause) {
    if (cause instanceof Exceptions.BadRequestException) {
      return HttpResponseStatus.BAD_REQUEST;
    }
    if (cause instanceof Exceptions.NotFoundException) {
      return HttpResponseStatus.NOT_FOUND;
    }
    if (cause instanceof SQLException) {
      if (cause.getMessage().contains("duplicate key value violates unique constraint")) {
        return HttpResponseStatus.BAD_REQUEST;
      }
      if (cause.getMessage().contains("violates foreign key constraint")) {
        return HttpResponseStatus.BAD_REQUEST;
      }
    }

    return HttpResponseStatus.INTERNAL_SERVER_ERROR;
  }
}
