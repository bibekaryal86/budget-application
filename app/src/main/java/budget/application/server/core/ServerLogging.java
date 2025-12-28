package budget.application.server.core;

import budget.application.common.Constants;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLogging extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(ServerLogging.class);

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object obj) throws Exception {
    if (obj instanceof FullHttpRequest req) {
      final String requestId = UUID.randomUUID().toString();
      // set requestId in channel handler context for later use
      ctx.channel().attr(Constants.REQUEST_ID).set(requestId);

      final String requestContentLength =
          req.headers().get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
      log.info(
          "[{}] Request IN: Method=[{}], Uri=[{}], ContentLength=[{}]",
          requestId,
          req.method(),
          req.uri(),
          requestContentLength);
    }
    super.channelRead(ctx, obj);
  }

  @Override
  public void write(
      final ChannelHandlerContext ctx, final Object obj, final ChannelPromise channelPromise)
      throws Exception {
    if (obj instanceof FullHttpResponse fullHttpResponse) {
      final String responseContentLength =
          fullHttpResponse
              .headers()
              .get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
      final HttpResponseStatus responseStatus = fullHttpResponse.status();
      final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();

      log.info(
          "[{}] Response OUT: Status=[{}], ContentLength=[{}]",
          requestId,
          responseStatus,
          responseContentLength);
    }
    super.write(ctx, obj, channelPromise);
  }
}
