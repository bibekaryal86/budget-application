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

  private final TransactionItemService transactionItemService;

  public TransactionItemHandler(DataSource dataSource) {
    this.transactionItemService = new TransactionItemService(dataSource);
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ: GET /petssvc/api/v1/transaction-items/tags
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1_WITH_TAGS) && method.equals(HttpMethod.GET)) {
      handleReadTags(requestId, channelHandlerContext);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, channelHandlerContext);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleReadOne(requestId, channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleUpdate(requestId, channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID)
        && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
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
    TransactionItemRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionItemRequest.class);
    TransactionItemResponse response = transactionItemService.create(requestId, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext channelHandlerContext)
      throws Exception {
    TransactionItemResponse response = transactionItemService.read(requestId, List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    TransactionItemResponse response = transactionItemService.read(requestId, List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ TAGS
  private void handleReadTags(String requestId, ChannelHandlerContext channelHandlerContext)
      throws Exception {
    TransactionItemResponse.TransactionItemTags response =
        transactionItemService.readTransactionItemTags(requestId);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      FullHttpRequest fullHttpRequest,
      UUID id)
      throws Exception {
    TransactionItemRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionItemRequest.class);
    TransactionItemResponse response = transactionItemService.update(requestId, id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    TransactionItemResponse response = transactionItemService.delete(requestId, List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
