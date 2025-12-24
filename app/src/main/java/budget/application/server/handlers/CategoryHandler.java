package budget.application.server.handlers;

import budget.application.model.dto.request.CategoryRequest;
import budget.application.model.dto.response.CategoryResponse;
import budget.application.server.utils.ServerUtils;
import budget.application.service.domain.CategoryService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class CategoryHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String PREFIX = "/petssvc/api/v1/categories";

    private final CategoryService service;

    public CategoryHandler(CategoryService service) {
        this.service = service;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String path = req.uri();
        HttpMethod method = req.method();

        if (!path.startsWith(PREFIX)) {
            ctx.fireChannelRead(req.retain());
            return;
        }
        // TODO logging
        log.info("Request: {} {}", method, path);

        // CREATE: POST /petssvc/api/v1/category-types
        if (path.equals(PREFIX) && method.equals(HttpMethod.POST)) {
            handleCreate(ctx, req);
            return;
        }

        // READ ALL: GET /petssvc/api/v1/category-types
        if (path.equals(PREFIX) && method.equals(HttpMethod.GET)) {
            handleReadAll(ctx);
            return;
        }

        // READ ONE: GET /petssvc/api/v1/category-types/{id}
        if (path.startsWith(PREFIX + "/") && method.equals(HttpMethod.GET)) {
            String id = path.substring((PREFIX + "/").length());
            handleReadOne(ctx, ServerUtils.getId(id));
            return;
        }

        // UPDATE: PUT /petssvc/api/v1/category-types/{id}
        if (path.startsWith(PREFIX + "/") && method.equals(HttpMethod.PUT)) {
            String id = path.substring((PREFIX + "/").length());
            handleUpdate(ctx, req, ServerUtils.getId(id));
            return;
        }

        // DELETE: DELETE /petssvc/api/v1/category-types/{id}
        if (path.startsWith(PREFIX + "/") && method.equals(HttpMethod.DELETE)) {
            String id = path.substring((PREFIX + "/").length());
            handleDelete(ctx, ServerUtils.getId(id));
            return;
        }

        ctx.fireChannelRead(req.retain());
    }

    // CREATE
    private void handleCreate(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        CategoryRequest request = ServerUtils.getRequestBody(req, CategoryRequest.class);
        CategoryResponse response = service.create(request);
        ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
    }

    // READ ALL
    private void handleReadAll(ChannelHandlerContext ctx) throws Exception {
        CategoryResponse response = service.read(List.of());
        ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
    }

    // READ ONE
    private void handleReadOne(ChannelHandlerContext ctx, UUID id) throws Exception {
        CategoryResponse response = service.read(List.of(id));
        ServerUtils.sendResponse(ctx, HttpResponseStatus.CREATED, response);
    }

    // UPDATE
    private void handleUpdate(ChannelHandlerContext ctx, FullHttpRequest req, UUID id) throws Exception {
        CategoryRequest request = ServerUtils.getRequestBody(req, CategoryRequest.class);
        CategoryResponse response = service.update(id, request);
        ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
    }

    // DELETE
    private void handleDelete(ChannelHandlerContext ctx, UUID id) throws Exception {
        CategoryResponse response = service.delete(List.of(id));
        ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
    }
}
