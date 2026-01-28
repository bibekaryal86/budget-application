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
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryTypeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(CategoryTypeHandler.class);

  private final CategoryTypeService service;

  public CategoryTypeHandler(DataSource dataSource) {
    this.service = new CategoryTypeService(dataSource);
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    String path = fullHttpRequest.uri();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.CATEGORY_TYPES_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // CREATE: POST /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(requestId, channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/category-types
    if (path.equals(ApiPaths.CATEGORY_TYPES_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(requestId, channelHandlerContext);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleReadOne(requestId, channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
      handleUpdate(requestId, channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/category-types/{id}
    if (path.startsWith(ApiPaths.CATEGORY_TYPES_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.CATEGORY_TYPES_V1_WITH_ID);
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
    CategoryTypeRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, CategoryTypeRequest.class);
    CategoryTypeResponse response = service.create(requestId, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(String requestId, ChannelHandlerContext channelHandlerContext)
      throws Exception {
    CategoryTypeResponse response = service.read(requestId, List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    CategoryTypeResponse response = service.read(requestId, List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      FullHttpRequest fullHttpRequest,
      UUID id)
      throws Exception {
    CategoryTypeRequest request =
        ServerUtils.getRequestBody(fullHttpRequest, CategoryTypeRequest.class);
    CategoryTypeResponse response = service.update(requestId, id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(String requestId, ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    CategoryTypeResponse response = service.delete(requestId, List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
