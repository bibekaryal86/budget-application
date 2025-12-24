package budget.application.server.handlers;

import budget.application.server.utils.ServerUtils;
import budget.application.utilities.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppTestsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final String PREFIX = "/petssvc/tests/";

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();
    HttpMethod method = req.method();

    if (!path.startsWith(PREFIX)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/category-types
    if (path.equals(PREFIX + "ping") && method.equals(HttpMethod.GET)) {
      handlePing(requestId, ctx);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // TESTS PING
  private void handlePing(String requestId, ChannelHandlerContext ctx) {
    Map<String, String> response = Map.of("ping", String.format("[%s] successful", requestId));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }
}
