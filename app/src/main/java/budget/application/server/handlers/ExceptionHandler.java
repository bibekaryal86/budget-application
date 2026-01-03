package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.server.util.ServerUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {
  private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    final String className = cause.getClass().getSimpleName();
    String message = cause.getMessage();

    if (message.contains(requestId)) {
      message = message.replace(requestId, "");
      message = message.replace("[]", "");
    }

    log.error("[{}] Exception caught", requestId, cause);
    ResponseWithMetadata response =
        ServerUtils.getResponseWithMetadata(
            String.format("[%s] [%s]--[%s]", requestId, className, message));
    ServerUtils.sendResponse(ctx, getHttpStatus(cause), response);
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
