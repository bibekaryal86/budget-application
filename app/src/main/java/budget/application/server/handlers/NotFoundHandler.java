package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.server.util.ServerUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

public class NotFoundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    ResponseWithMetadata response =
        ServerUtils.getResponseWithMetadata(
            String.format("[%s] The requested resource does not exist...", requestId));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.NOT_FOUND, response);
  }
}
