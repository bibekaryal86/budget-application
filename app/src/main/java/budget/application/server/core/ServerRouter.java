package budget.application.server.core;

import budget.application.server.handlers.CategoryHandler;
import budget.application.server.handlers.CategoryTypeHandler;
import budget.application.server.handlers.TransactionItemHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import javax.sql.DataSource;

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
    String path = req.uri();

    if (path.startsWith("/petssvc/api/v1/category-types")) {
      categoryTypeHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith("/petssvc/api/v1/categories")) {
      categoryHandler.channelRead(ctx, req.retain());
      return;
    }

    if (path.startsWith("/petssvc/api/v1/transaction-items")) {
      transactionItemHandler.channelRead(ctx, req.retain());
      return;
    }

    ctx.fireChannelRead(req.retain());
  }
}
