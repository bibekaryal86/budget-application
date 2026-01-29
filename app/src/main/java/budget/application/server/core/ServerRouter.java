package budget.application.server.core;

import budget.application.server.handlers.AccountHandler;
import budget.application.server.handlers.AppTestsHandler;
import budget.application.server.handlers.BudgetHandler;
import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.InsightsHandler;
import budget.application.server.handlers.TransactionHandler;
import budget.application.server.handlers.TransactionItemHandler;
import budget.application.server.util.ApiPaths;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(ServerRouter.class);

  private final AppTestsHandler appTestsHandler;
  private final AccountHandler accountHandler;
  private final BudgetHandler budgetHandler;
  private final CategoryTypeHandler categoryTypeHandler;
  private final CategoryHandler categoryHandler;
  private final InsightsHandler insightsHandler;
  private final TransactionItemHandler transactionItemHandler;
  private final TransactionHandler transactionHandler;

  public ServerRouter(ServerManager serverManager) {
    this.appTestsHandler = serverManager.getAppTestsHandler();
    this.accountHandler = serverManager.getAccountHandler();
    this.budgetHandler = serverManager.getBudgetHandler();
    this.categoryTypeHandler = serverManager.getCategoryTypeHandler();
    this.categoryHandler = serverManager.getCategoryHandler();
    this.insightsHandler = serverManager.getInsightsHandler();
    this.transactionItemHandler = serverManager.getTransactionItemHandler();
    this.transactionHandler = serverManager.getTransactionHandler();
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String path = fullHttpRequest.uri();

    if (path.startsWith(ApiPaths.APP_TESTS)) {
      log.info("Routing to AppTestsHandler: [{}]", path);
      appTestsHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.ACCOUNTS_V1)) {
      log.info("Routing to AccountHandler: [{}]", path);
      accountHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.BUDGETS_V1)) {
      log.info("Routing to BudgetHandler: [{}]", path);
      budgetHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.CATEGORIES_V1)) {
      log.info("Routing to CategoryHandler: [{}]", path);
      categoryHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1)) {
      log.info("Routing to CategoryTypeHandler: [{}]", path);
      categoryTypeHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.INSIGHTS_V1)) {
      log.info("Routing to InsightsHandler: [{}]", path);
      insightsHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.TRANSACTIONS_V1)) {
      log.info("Routing to TransactionHandler: [{}]", path);
      transactionHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1)) {
      log.info("Routing to TransactionItemHandler: [{}]", path);
      transactionItemHandler.channelRead(channelHandlerContext, fullHttpRequest.retain());
      return;
    }

    log.info("Handler Not Found in ServerRouter: [{}]", path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }
}
