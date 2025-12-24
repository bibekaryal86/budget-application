package budget.application.server.handlers;

import budget.application.server.utils.ServerUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

public class NotFoundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    ResponseWithMetadata response =
        ServerUtils.getResponseWithMetadata("The requested resource does not exist...");
    ServerUtils.sendResponse(ctx, HttpResponseStatus.NOT_FOUND, response);
  }
}
