package budget.application.server.handlers;

import budget.application.model.dto.CategoryRequest;
import budget.application.model.dto.CategoryResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.CategoryService;
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

public class CategoryHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(CategoryHandler.class);

  private final CategoryService categoryService;

  public CategoryHandler(DataSource dataSource) {
    this.categoryService = new CategoryService(dataSource);
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.CATEGORIES_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // CREATE: POST /petssvc/api/v1/categories
    if (path.equals(ApiPaths.CATEGORIES_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/categories
    if (path.equals(ApiPaths.CATEGORIES_V1) && method.equals(HttpMethod.GET)) {
      List<UUID> catTypeIds = ServerUtils.getCategoryParams(decoder).categoryTypeIds();
      handleReadAll(channelHandlerContext, catTypeIds);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
      handleReadOne(channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
      handleUpdate(channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/categories/{id}
    if (path.startsWith(ApiPaths.CATEGORIES_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORIES_V1_WITH_ID);
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
    CategoryRequest request = ServerUtils.getRequestBody(fullHttpRequest, CategoryRequest.class);
    CategoryResponse response = categoryService.create(request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(ChannelHandlerContext channelHandlerContext, List<UUID> catTypeIds)
      throws Exception {
    CategoryResponse response = categoryService.read(List.of(), catTypeIds);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    CategoryResponse response = categoryService.read(List.of(id), List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, UUID id)
      throws Exception {
    CategoryRequest request = ServerUtils.getRequestBody(fullHttpRequest, CategoryRequest.class);
    CategoryResponse response = categoryService.update(id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(ChannelHandlerContext channelHandlerContext, UUID id) throws Exception {
    CategoryResponse response = categoryService.delete(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
