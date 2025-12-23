package budget.application.server.core;

import budget.application.utilities.Constants;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerLogging extends ChannelDuplexHandler {

  @Override
  public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object object)
      throws Exception {
    if (object instanceof FullHttpRequest fullHttpRequest) {
      final String requestId = UUID.randomUUID().toString();

      final String requestContentLength =
          fullHttpRequest
              .headers()
              .get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
      log.info(
          "[{}] Request IN: Method=[{}], Uri=[{}], ContentLength=[{}]",
          requestId,
          fullHttpRequest.method(),
          fullHttpRequest.uri(),
          requestContentLength);

      // set requestId in channel handler context for later use
      channelHandlerContext.channel().attr(Constants.REQUEST_ID).set(requestId);
    }
    super.channelRead(channelHandlerContext, object);
  }

  @Override
  public void write(
      final ChannelHandlerContext channelHandlerContext,
      final Object object,
      final ChannelPromise channelPromise)
      throws Exception {
    if (object instanceof FullHttpResponse fullHttpResponse) {
      final String responseContentLength =
          fullHttpResponse
              .headers()
              .get(HttpHeaderNames.CONTENT_LENGTH, Constants.CONTENT_LENGTH_DEFAULT);
      final HttpResponseStatus responseStatus = fullHttpResponse.status();
      final String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();

      log.info(
          "[{}] Response OUT: Status=[{}], ContentLength=[{}]",
          requestId,
          responseStatus,
          responseContentLength);
    }
    super.write(channelHandlerContext, object, channelPromise);
  }
}
