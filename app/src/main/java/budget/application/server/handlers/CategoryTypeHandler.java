package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.model.dto.response.CategoryTypeResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.ServerUtils;
import budget.application.service.domain.CategoryTypeService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CategoryTypeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final CategoryTypeService service;

  public CategoryTypeHandler(DataSource dataSource) {
    this.service = new CategoryTypeService(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.CATEGORY_TYPES_V1)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, ctx, req);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, ctx);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getRequestId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleReadOne(requestId, ctx, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getRequestId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleUpdate(requestId, ctx, req, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getRequestId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleDelete(requestId, ctx, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // CREATE
  private void handleCreate(String requestId, ChannelHandlerContext ctx, FullHttpRequest req)
      throws Exception {
    CategoryTypeRequest request = ServerUtils.getRequestBody(req, CategoryTypeRequest.class);
    CategoryTypeResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext ctx) throws Exception {
    CategoryTypeResponse response = service.read(requestId, List.of());
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext ctx, UUID id)
      throws Exception {
    CategoryTypeResponse response = service.read(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId, ChannelHandlerContext ctx, FullHttpRequest req, UUID id) throws Exception {
    CategoryTypeRequest request = ServerUtils.getRequestBody(req, CategoryTypeRequest.class);
    CategoryTypeResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext ctx, UUID id) throws Exception {
    CategoryTypeResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
