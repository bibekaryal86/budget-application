package budget.application.server.core;

import budget.application.server.utils.ServerUtils;
import budget.application.utilities.Constants;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerSecurity extends ChannelDuplexHandler {

  @Override
  public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object object)
      throws Exception {
    if (object instanceof FullHttpRequest fullHttpRequest) {
      final String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
      final String requestUri = fullHttpRequest.uri();

      final boolean isNoAuth = isNoAuthCheck(requestUri);
      if (isNoAuth) {
        log.debug("[{}] No Auth Request...", requestId);
        super.channelRead(channelHandlerContext, fullHttpRequest);
        return;
      }

      String authHeader = fullHttpRequest.headers().get(HttpHeaderNames.AUTHORIZATION);
      if (CommonUtilities.isEmpty(authHeader)) {
        authHeader = fullHttpRequest.headers().get(HttpHeaderNames.AUTHORIZATION.toLowerCase());
      }

      if (CommonUtilities.isEmpty(authHeader)) {
        log.error("[{}] No Auth Header...", requestId);
        ServerUtils.sendResponse(
            channelHandlerContext, HttpResponseStatus.UNAUTHORIZED, "Not Authenticated...");
        return;
      }

      if (!isBasicAuthenticated(authHeader)) {
        ServerUtils.sendResponse(
            channelHandlerContext, HttpResponseStatus.UNAUTHORIZED, "Not Authorized...");
        return;
      }
    }

    super.channelRead(channelHandlerContext, object);
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
