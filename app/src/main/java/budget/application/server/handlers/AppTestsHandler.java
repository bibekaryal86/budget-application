package budget.application.server.handlers;

import budget.application.scheduler.ScheduleManager;
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

  private final ScheduleManager scheduleManager;

  public AppTestsHandler(ScheduleManager scheduleManager) {
    this.scheduleManager = scheduleManager;
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String path = fullHttpRequest.uri();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.APP_TESTS)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // PING: GET /petssvc/tests/ping
    if (path.equals(ApiPaths.APP_TESTS_PING) && method.equals(HttpMethod.GET)) {
      handlePing(channelHandlerContext);
      return;
    }

    // PING: GET /petssvc/tests/schedulers
    if (path.equals(ApiPaths.APP_TESTS_SCHEDULERS) && method.equals(HttpMethod.GET)) {
      handleSchedulers(channelHandlerContext);
      return;
    }

    log.info("Action Not Found: Method=[{}] Path=[{}]", method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // TESTS PING
  private void handlePing(ChannelHandlerContext channelHandlerContext) {
    Map<String, String> response = Map.of("ping", "successful");
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // SCHEDULERS STATUS
  private void handleSchedulers(ChannelHandlerContext channelHandlerContext) {
    Map<String, Object> response = scheduleManager.getSchedulerStatus();
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
