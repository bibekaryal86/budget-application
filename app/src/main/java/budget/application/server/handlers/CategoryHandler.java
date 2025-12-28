package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.server.utils.ApiPaths;
import budget.application.server.utils.ServerUtils;
import budget.application.service.domain.CategoryService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(CategoryHandler.class);

  private final CategoryService service;

  public CategoryHandler(DataSource dataSource) {
    this.service = new CategoryService(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    String path = req.uri();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.CATEGORIES_V1)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/categories
    if (path.equals(ApiPaths.CATEGORIES_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, ctx, req);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/categories
    if (path.equals(ApiPaths.CATEGORIES_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, ctx);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
      handleReadOne(requestId, ctx, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
      handleUpdate(requestId, ctx, req, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
      handleDelete(requestId, ctx, id);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // CREATE
  private void handleCreate(String requestId, ChannelHandlerContext ctx, FullHttpRequest req)
      throws Exception {
    CategoryRequest request = ServerUtils.getRequestBody(req, CategoryRequest.class);
    CategoryResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext ctx) throws Exception {
    CategoryResponse response = service.read(requestId, List.of());
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext ctx, UUID id)
      throws Exception {
    CategoryResponse response = service.read(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId, ChannelHandlerContext ctx, FullHttpRequest req, UUID id) throws Exception {
    CategoryRequest request = ServerUtils.getRequestBody(req, CategoryRequest.class);
    CategoryResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext ctx, UUID id) throws Exception {
    CategoryResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
