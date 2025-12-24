package budget.application.server.core;

import budget.application.utilities.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class ServerRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // TODO change this to same as gateway-service
        final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    }
}
