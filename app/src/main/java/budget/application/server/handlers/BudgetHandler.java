package budget.application.server.handlers;

import budget.application.model.dto.BudgetRequest;
import budget.application.model.dto.BudgetResponse;
import budget.application.model.dto.RequestParams;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.BudgetService;
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

public class BudgetHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(BudgetHandler.class);

  private final BudgetService budgetService;

  public BudgetHandler(DataSource dataSource) {
    this.budgetService = new BudgetService(dataSource);
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.BUDGETS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // CREATE: POST /petssvc/api/v1/budgets
    if (path.equals(ApiPaths.BUDGETS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/budgets
    if (path.equals(ApiPaths.BUDGETS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(channelHandlerContext, decoder);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/budgets/{id}
    if (path.startsWith(ApiPaths.BUDGETS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.BUDGETS_V1_WITH_ID);
      handleReadOne(channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/budgets/{id}
    if (path.startsWith(ApiPaths.BUDGETS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.BUDGETS_V1_WITH_ID);
      handleUpdate(channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/budgets/{id}
    if (path.startsWith(ApiPaths.BUDGETS_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.BUDGETS_V1_WITH_ID);
      handleDelete(channelHandlerContext, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // CREATE
  private void handleCreate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    BudgetRequest request = ServerUtils.getRequestBody(fullHttpRequest, BudgetRequest.class);
    BudgetResponse response = budgetService.create(request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(
      ChannelHandlerContext channelHandlerContext, QueryStringDecoder decoder) throws Exception {
    RequestParams.BudgetParams params = ServerUtils.getBudgetParams(decoder);
    BudgetResponse response = budgetService.read(List.of(), params);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    BudgetResponse response =
        budgetService.read(List.of(id), new RequestParams.BudgetParams(0, 0, List.of()));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, UUID id)
      throws Exception {
    BudgetRequest request = ServerUtils.getRequestBody(fullHttpRequest, BudgetRequest.class);
    BudgetResponse response = budgetService.update(id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(ChannelHandlerContext channelHandlerContext, UUID id) throws Exception {
    BudgetResponse response = budgetService.delete(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
