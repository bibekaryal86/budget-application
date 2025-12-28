package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.ServerUtils;
import budget.application.service.domain.TransactionService;
import io.github.bibekaryal86.shdsvc.Email;
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

public class TransactionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

  private final TransactionService service;

  public TransactionHandler(DataSource dataSource, Email email) {
    this.service = new TransactionService(dataSource, email);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = decoder.path();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.TRANSACTIONS_V1)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/transactions
    if (path.equals(ApiPaths.TRANSACTIONS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, ctx, req);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/transactions
    if (path.equals(ApiPaths.TRANSACTIONS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, ctx);
      return;
    }

    if (path.equals(ApiPaths.TRANSACTIONS_V1_WITH_MERCHANTS) && method.equals(HttpMethod.GET)) {
      handleReadMerchants(requestId, ctx);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleReadOne(requestId, ctx, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleUpdate(requestId, ctx, req, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleDelete(requestId, ctx, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // CREATE
  private void handleCreate(String requestId, ChannelHandlerContext ctx, FullHttpRequest req)
      throws Exception {
    TransactionRequest request = ServerUtils.getRequestBody(req, TransactionRequest.class);
    TransactionResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext ctx) throws Exception {
    TransactionResponse response = service.read(requestId, List.of());
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext ctx, UUID id)
      throws Exception {
    TransactionResponse response = service.read(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  private void handleReadMerchants(String requestId, ChannelHandlerContext ctx) throws Exception {
    TransactionResponse response = service.readTransactionMerchants(requestId);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId, ChannelHandlerContext ctx, FullHttpRequest req, UUID id) throws Exception {
    TransactionRequest request = ServerUtils.getRequestBody(req, TransactionRequest.class);
    TransactionResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext ctx, UUID id) throws Exception {
    TransactionResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
