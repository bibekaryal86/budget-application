package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTestsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(AppTestsHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.APP_TESTS)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // PING: GET /petssvc/tests/ping
    if (path.equals(ApiPaths.APP_TESTS_PING) && method.equals(HttpMethod.GET)) {
      handlePing(requestId, ctx);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // TESTS PING
  private void handlePing(String requestId, ChannelHandlerContext ctx) {
    Map<String, String> response = Map.of("ping", String.format("[%s] successful", requestId));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
