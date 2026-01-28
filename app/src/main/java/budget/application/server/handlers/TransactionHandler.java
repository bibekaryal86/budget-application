package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.PaginationRequest;
import budget.application.model.dto.RequestParams;
import budget.application.model.dto.TransactionRequest;
import budget.application.model.dto.TransactionResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
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
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.TRANSACTIONS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/transactions
    if (path.equals(ApiPaths.TRANSACTIONS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ: GET /petssvc/api/v1/transactions/merchants
    if (path.equals(ApiPaths.TRANSACTIONS_V1_WITH_MERCHANTS) && method.equals(HttpMethod.GET)) {
      handleReadMerchants(requestId, channelHandlerContext);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/transactions
    if (path.equals(ApiPaths.TRANSACTIONS_V1) && method.equals(HttpMethod.GET)) {
      RequestParams.TransactionParams params = ServerUtils.getTransactionParams(decoder);
      PaginationRequest paginationRequest = ServerUtils.getPaginationRequest(decoder);
      handleReadAll(requestId, channelHandlerContext, params, paginationRequest);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleReadOne(requestId, channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleUpdate(requestId, channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/transactions/{id}
    if (path.startsWith(ApiPaths.TRANSACTIONS_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTIONS_V1_WITH_ID);
      handleDelete(requestId, channelHandlerContext, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // CREATE
  private void handleCreate(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      FullHttpRequest fullHttpRequest)
      throws Exception {
    TransactionRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionRequest.class);
    TransactionResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ MERCHANTS
  private void handleReadMerchants(String requestId, ChannelHandlerContext channelHandlerContext)
      throws Exception {
    TransactionResponse.TransactionMerchants response = service.readTransactionMerchants(requestId);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ALL
  private void handleReadAll(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      RequestParams.TransactionParams params,
      PaginationRequest paginationRequest)
      throws Exception {
    TransactionResponse response = service.read(requestId, List.of(), params, paginationRequest);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    TransactionResponse response = service.read(requestId, List.of(id), null, null);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      FullHttpRequest fullHttpRequest,
      UUID id)
      throws Exception {
    TransactionRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionRequest.class);
    TransactionResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    TransactionResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
