package budget.application.server.handlers;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(InsightsHandler.class);

  private final InsightsService insightsService;

  public InsightsHandler(InsightsService insightsService) {
    this.insightsService = insightsService;
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.INSIGHTS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // READ: GET /petssvc/api/v1/insights/cf-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CF_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CashFlowSummaryParams params = ServerUtils.getCashFlowSummaryParams(decoder);
      handleCashFlowSummaries(channelHandlerContext, params);
      return;
    }

    // READ: GET /petssvc/api/v1/insights/cat-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CategorySummaryParams params = ServerUtils.getCategorySummaryParams(decoder);
      handleCategorySummaries(channelHandlerContext, params);
      return;
    }

    log.info("Action Not Found: Method=[{}] Path=[{}]", method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // READ TXN SUMMARIES
  private void handleCashFlowSummaries(
      ChannelHandlerContext channelHandlerContext, RequestParams.CashFlowSummaryParams params)
      throws Exception {
    InsightsResponse.CashFlowSummaries response = insightsService.readCashFLowSummaries(params);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ CAT SUMMARIES
  private void handleCategorySummaries(
      ChannelHandlerContext channelHandlerContext, RequestParams.CategorySummaryParams params)
      throws Exception {
    InsightsResponse.CategorySummaries response = insightsService.readCategoriesSummary(params);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
