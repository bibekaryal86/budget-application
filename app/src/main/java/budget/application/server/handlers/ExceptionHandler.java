package budget.application.server.handlers;

import budget.application.common.Constants;
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
    ServerUtils.sendResponse(ctx, HttpResponseStatus.NOT_FOUND, response);
    ctx.close();
  }
}
