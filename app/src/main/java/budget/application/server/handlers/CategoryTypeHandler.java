package budget.application.server.handlers;

import budget.application.model.dto.request.CategoryTypeRequest;
import budget.application.server.utils.Endpoints;
import budget.application.service.domain.CategoryTypeService;
import budget.application.service.util.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CategoryTypeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final CategoryTypeService svc;

    public CategoryTypeHandler(CategoryTypeService svc) {
        this.svc = svc;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String path = req.uri();
        HttpMethod method = req.method();

        // POST /category-types
        if (path.equals(Endpoints.CATEGORY_TYPES) && method.equals(HttpMethod.POST)) {
            handleCreate(ctx, req);
            return;
        }

        // GET /category-types
        if (path.equals(Endpoints.CATEGORY_TYPES) && method.equals(HttpMethod.GET)) {
            handleReadAll(ctx);
            return;
        }

        // GET /category-types/{id}
        if (path.startsWith(Endpoints.CATEGORY_TYPES_ID) && method.equals(HttpMethod.GET)) {
            handleReadOne(ctx, id);
            return;
        }

        // PUT /category-types/{id}
        if (path.startsWith(Endpoints.CATEGORY_TYPES_ID) && method.equals(HttpMethod.PUT)) {
            String id = path.substring(Endpoints.CATEGORY_TYPES_ID.length());
            handleUpdate(ctx, req, id);
            return;
        }

        // DELETE /category-types/{id}
        if (path.startsWith(Endpoints.CATEGORY_TYPES_ID) && method.equals(HttpMethod.DELETE)) {
            String id = path.substring(Endpoints.CATEGORY_TYPES_ID.length());
            handleDelete(ctx, id);
            return;
        }

        ctx.fireChannelRead(req.retain());
    }

    private void handleCreate(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        String body = req.content().toString(CharsetUtil.UTF_8);
        CategoryTypeRequest request = JsonUtils.fromJson(body, CategoryTypeRequest.class);


        var created = repo.create(createReq);
        writeJson(ctx, HttpResponseStatus.CREATED, created);
    }

    private void handleReadAll(ChannelHandlerContext ctx) throws Exception {
        var list = svc.read(List.of());
        writeJson(ctx, HttpResponseStatus.OK, list);
    }

    private void handleUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String id) throws Exception {
        String body = req.content().toString(io.netty.util.CharsetUtil.UTF_8);
        var updateReq = json.deserialize(body, CreateCategoryTypeRequest.class);

        var updated = repo.update(id, updateReq);
        writeJson(ctx, HttpResponseStatus.OK, updated);
    }

    private void handleDelete(ChannelHandlerContext ctx, String id) throws Exception {
        repo.delete(id);
        writeJson(ctx, HttpResponseStatus.NO_CONTENT, "");
    }

    private void writeJson(ChannelHandlerContext ctx, HttpResponseStatus status, Object body) {
        String jsonBody = json.serialize(body);

        FullHttpResponse resp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                io.netty.buffer.Unpooled.copiedBuffer(jsonBody, io.netty.util.CharsetUtil.UTF_8)
        );

        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, jsonBody.length());

        ctx.writeAndFlush(resp);
    }
}

