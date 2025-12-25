package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.common.Exceptions;
import budget.application.server.utils.ServerUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    final String className = cause.getClass().getSimpleName();
    final String message = cause.getMessage();
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
    return HttpResponseStatus.INTERNAL_SERVER_ERROR;
  }
}
