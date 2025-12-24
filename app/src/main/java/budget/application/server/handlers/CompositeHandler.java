package budget.application.server.handlers;

import budget.application.model.dto.request.CompositeRequest;
import budget.application.model.dto.response.CompositeResponse;
import budget.application.server.utils.ServerUtils;
import budget.application.service.domain.CompositeService;
import budget.application.utilities.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompositeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final String PREFIX = "/petssvc/api/v1/composites";

  private final CompositeService service;

  public CompositeHandler(DataSource dataSource) {
    this.service = new CompositeService(dataSource);
  }

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

    // Transactions: POST /petssvc/api/v1/composites/transactions
    if (path.equals(PREFIX + "transactions") && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, ctx, req);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // CREATE
  private void handleCreate(String requestId, ChannelHandlerContext ctx, FullHttpRequest req)
      throws Exception {
    CompositeRequest request = ServerUtils.getRequestBody(req, CompositeRequest.class);
    CompositeResponse response = service.compositeTransactions(requestId, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }
}
