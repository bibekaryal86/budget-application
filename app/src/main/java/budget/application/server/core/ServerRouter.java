package budget.application.server.core;

import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.TransactionItemHandler;
import budget.application.utilities.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
  private final CategoryTypeHandler categoryTypeHandler;
  private final CategoryHandler categoryHandler;
  private final TransactionItemHandler transactionItemHandler;

  public ServerRouter(DataSource dataSource) {
    this.categoryTypeHandler = new CategoryTypeHandler(dataSource);
    this.categoryHandler = new CategoryHandler(dataSource);
    this.transactionItemHandler = new TransactionItemHandler(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();

    if (path.startsWith("/petssvc/api/v1/category-types")) {
      log.info("[{}] Routing to CategoryTypeHandler: {}", requestId, path);
      categoryTypeHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith("/petssvc/api/v1/categories")) {
      log.info("[{}] Routing to CategoryHandler: {}", requestId, path);
      categoryHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith("/petssvc/api/v1/transaction-items")) {
      log.info("[{}] Routing to TransactionItemHandler: {}", requestId, path);
      transactionItemHandler.channelRead(ctx, req.retain());
      return;
    }

    log.info("[{}] Handler Not Found in ServerRouter: {}", requestId, path);
    ctx.fireChannelRead(req.retain());
  }
}
