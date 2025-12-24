package budget.application.server.core;

import budget.application.server.utils.ServerUtils;
import budget.application.utilities.Constants;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerSecurity extends ChannelDuplexHandler {

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object obj) throws Exception {
    if (obj instanceof FullHttpRequest req) {
      final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
      final String requestUri = req.uri();

      final boolean isNoAuth = isNoAuthCheck(requestUri);
      if (isNoAuth) {
        log.debug("[{}] No Auth Request...", requestId);
        super.channelRead(ctx, req);
        return;
      }

      String authHeader = req.headers().get(HttpHeaderNames.AUTHORIZATION);
      if (CommonUtilities.isEmpty(authHeader)) {
        authHeader = req.headers().get(HttpHeaderNames.AUTHORIZATION.toLowerCase());
      }

      if (CommonUtilities.isEmpty(authHeader)) {
        log.error("[{}] No Auth Header...", requestId);
        ResponseWithMetadata response = ServerUtils.getResponseWithMetadata("Not Authenticated...");
        ServerUtils.sendResponse(ctx, HttpResponseStatus.UNAUTHORIZED, response);
        return;
      }

      if (!isBasicAuthenticated(authHeader)) {
        ResponseWithMetadata response = ServerUtils.getResponseWithMetadata("Not Authorized...");
        ServerUtils.sendResponse(ctx, HttpResponseStatus.UNAUTHORIZED, response);
        return;
      }
    }

    super.channelRead(ctx, obj);
  }

  private boolean isNoAuthCheck(final String requestUri) {
    return requestUri.matches("^.*/tests/ping.*");
  }

  private boolean isBasicAuthenticated(final String actualAuth) {
    final String username = CommonUtilities.getSystemEnvProperty(Constants.ENV_SELF_USERNAME);
    final String password = CommonUtilities.getSystemEnvProperty(Constants.ENV_SELF_PASSWORD);
    final String expectedAuth = CommonUtilities.getBasicAuth(username, password);
    return Objects.equals(expectedAuth, actualAuth);
  }
}
