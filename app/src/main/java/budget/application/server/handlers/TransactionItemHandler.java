package budget.application.server.handlers;

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
    // String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // CREATE: POST /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ: GET /petssvc/api/v1/transaction-items/tags
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1_WITH_TAGS) && method.equals(HttpMethod.GET)) {
      handleReadTags(channelHandlerContext);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/transaction-items
    if (path.equals(ApiPaths.TRANSACTION_ITEMS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(channelHandlerContext);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleReadOne(channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleUpdate(channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/transaction-items/{id}
    if (path.startsWith(ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID)
        && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.TRANSACTION_ITEMS_V1_WITH_ID);
      handleDelete(channelHandlerContext, id);
      return;
    }

    log.info("Action Not Found: Method=[{}] Path=[{}]", method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // CREATE
  private void handleCreate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    TransactionItemRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionItemRequest.class);
    TransactionItemResponse response = transactionItemService.create(request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(ChannelHandlerContext channelHandlerContext) throws Exception {
    TransactionItemResponse response = transactionItemService.read(List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    TransactionItemResponse response = transactionItemService.read(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ TAGS
  private void handleReadTags(ChannelHandlerContext channelHandlerContext) throws Exception {
    TransactionItemResponse.TransactionItemTags response =
        transactionItemService.readTransactionItemTags();
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, UUID id)
      throws Exception {
    TransactionItemRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, TransactionItemRequest.class);
    TransactionItemResponse response = transactionItemService.update(id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(ChannelHandlerContext channelHandlerContext, UUID id) throws Exception {
    TransactionItemResponse response = transactionItemService.delete(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
