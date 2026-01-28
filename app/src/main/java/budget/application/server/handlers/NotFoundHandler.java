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
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    final String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    ResponseWithMetadata response =
        ServerUtils.getResponseWithMetadata(
            String.format("[%s] The requested resource does not exist...", requestId));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.NOT_FOUND, response);
  }
}
