package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.TransactionItemRequest;
import budget.application.model.dto.TransactionItemResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.TransactionItemService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionItemHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(TransactionItemHandler.class);

  private final TransactionItemService service;

  public TransactionItemHandler(DataSource dataSource) {
    this.service = new TransactionItemService(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = decoder.path();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, ctx, req);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, ctx);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleReadOne(requestId, ctx, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleUpdate(requestId, ctx, req, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID)
        && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleDelete(requestId, ctx, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // CREATE
  private void handleCreate(String requestId, ChannelHandlerContext ctx, FullHttpRequest req)
      throws Exception {
    TransactionItemRequest request = ServerUtils.getRequestBody(req, TransactionItemRequest.class);
    TransactionItemResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext ctx) throws Exception {
    TransactionItemResponse response = service.read(requestId, List.of());
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext ctx, UUID id)
      throws Exception {
    TransactionItemResponse response = service.read(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId, ChannelHandlerContext ctx, FullHttpRequest req, UUID id) throws Exception {
    TransactionItemRequest request = ServerUtils.getRequestBody(req, TransactionItemRequest.class);
    TransactionItemResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext ctx, UUID id) throws Exception {
    TransactionItemResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
