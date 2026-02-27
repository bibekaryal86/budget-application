package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.CategoryTypeRequest;
import budget.application.model.dto.CategoryTypeResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.CategoryTypeService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryTypeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(CategoryTypeHandler.class);

  private final CategoryTypeService categoryTypeService;

  public CategoryTypeHandler(CategoryTypeService categoryTypeService) {
    this.categoryTypeService = categoryTypeService;
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String path = fullHttpRequest.uri();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.CATEGORY_TYPES_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // CREATE: POST /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(channelHandlerContext);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleReadOne(channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleUpdate(channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
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
    CategoryTypeRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, CategoryTypeRequest.class);
    CategoryTypeResponse response = categoryTypeService.create(request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(ChannelHandlerContext channelHandlerContext) throws Exception {
    CategoryTypeResponse response = categoryTypeService.read(List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    CategoryTypeResponse response = categoryTypeService.read(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, UUID id)
      throws Exception {
    CategoryTypeRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, CategoryTypeRequest.class);
    CategoryTypeResponse response = categoryTypeService.update(id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(ChannelHandlerContext channelHandlerContext, UUID id) throws Exception {
    CategoryTypeResponse response = categoryTypeService.delete(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
