package budget.application.server.core;

import budget.application.common.Constants;
import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.TransactionHandler;
import budget.application.server.handlers.TransactionItemHandler;
import budget.application.server.util.ApiPaths;
import io.github.bibekaryal86.shdsvc.Email;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(ServerRouter.class);

  private final AppTestsHandler appTestsHandler;
  private final AccountHandler accountHandler;
  private final CategoryTypeHandler categoryTypeHandler;
  private final CategoryHandler categoryHandler;
  private final TransactionItemHandler transactionItemHandler;
  private final TransactionHandler transactionHandler;

  public ServerRouter(DataSource dataSource, Email email) {
    this.appTestsHandler = new AppTestsHandler();
    this.accountHandler = new AccountHandler(dataSource);
    this.categoryTypeHandler = new CategoryTypeHandler(dataSource);
    this.categoryHandler = new CategoryHandler(dataSource);
    this.transactionItemHandler = new TransactionItemHandler(dataSource);
    this.transactionHandler = new TransactionHandler(dataSource, email);
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

    if (path.startsWith(ApiPaths.ACCOUNTS_V1)) {
      log.info("[{}] Routing to AccountHandler: [{}]", requestId, path);
      accountHandler.channelRead(ctx, req.retain());
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
