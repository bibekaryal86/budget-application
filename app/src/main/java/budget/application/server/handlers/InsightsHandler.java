package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.InsightsResponse;
import budget.application.model.dto.RequestParams;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.InsightsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(InsightsHandler.class);

  private final InsightsService service;

  public InsightsHandler(DataSource dataSource) {
    this.service = new InsightsService(dataSource);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    String requestId = ctx.channel().attr(Constants.REQUEST_ID).get();
    QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
    String path = decoder.path();
    HttpMethod method = req.method();

    if (!path.startsWith(ApiPaths.INSIGHTS_V1)) {
      ctx.fireChannelRead(req.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // READ: GET /petssvc/api/v1/insights/cf-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CF_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CashFlowSummaryParams params = ServerUtils.getTransactionSummaryParams(decoder);
      handleCashFlowSummaries(requestId, ctx, params);
      return;
    }

    // READ: GET /petssvc/api/v1/insights/cat-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CategorySummaryParams params = ServerUtils.getCategorySummaryParams(decoder);
      handleCategorySummaries(requestId, ctx, params);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    ctx.fireChannelRead(req.retain());
  }

  // READ TXN SUMMARIES
  private void handleCashFlowSummaries(
      String requestId, ChannelHandlerContext ctx, RequestParams.CashFlowSummaryParams params)
      throws Exception {
    InsightsResponse.CashFlowSummaries response = service.readCashFLowSummaries(requestId, params);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }

  // READ CAT SUMMARIES
  private void handleCategorySummaries(
      String requestId, ChannelHandlerContext ctx, RequestParams.CategorySummaryParams params)
      throws Exception {
    InsightsResponse.CategorySummaries response = service.readCategoriesSummary(requestId, params);
    ServerUtils.sendResponse(ctx, HttpResponseStatus.OK, response);
  }
}
