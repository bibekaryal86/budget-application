package budget.application.server.core;

import budget.application.common.Constants;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
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
}
