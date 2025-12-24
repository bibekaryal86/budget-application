package budget.application.server.core;

import budget.application.common.Constants;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.CompositeHandler;
import budget.application.server.handlers.TransactionHandler;
import budget.application.server.handlers.TransactionItemHandler;
import budget.application.server.utils.ApiPaths;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
  private final AppTestsHandler appTestsHandler;
  private final CategoryTypeHandler categoryTypeHandler;
  private final CategoryHandler categoryHandler;
  private final TransactionItemHandler transactionItemHandler;
  private final TransactionHandler transactionHandler;
  private final CompositeHandler compositeHandler;

  public ServerRouter(DataSource dataSource) {
    this.appTestsHandler = new AppTestsHandler();
    this.categoryTypeHandler = new CategoryTypeHandler(dataSource);
    this.categoryHandler = new CategoryHandler(dataSource);
    this.transactionItemHandler = new TransactionItemHandler(dataSource);
    this.transactionHandler = new TransactionHandler(dataSource);
    this.compositeHandler = new CompositeHandler(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    final String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();

    if (path.startsWith(ApiPaths.APP_TESTS)) {
      log.info("[{}] Routing to AppTestsHandler: [{}]", requestId, path);
      appTestsHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith(ApiPaths.CATEGORIES_V1)) {
      log.info("[{}] Routing to CategoryHandler: [{}]", requestId, path);
      categoryHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1)) {
      log.info("[{}] Routing to CategoryTypeHandler: [{}]", requestId, path);
      categoryTypeHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith(ApiPaths.COMPOSITES_V1)) {
      log.info("[{}] Routing to CompositeHandler: [{}]", requestId, path);
      compositeHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith(ApiPaths.TRANSACTIONS_V1)) {
      log.info("[{}] Routing to TransactionHandler: [{}]", requestId, path);
      transactionHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1)) {
      log.info("[{}] Routing to TransactionItemHandler: [{}]", requestId, path);
      transactionItemHandler.channelRead(ctx, req.retain());
      return;
    }

    log.info("[{}] Handler Not Found in ServerRouter: [{}]", requestId, path);
    ctx.fireChannelRead(req.retain());
  }
}
