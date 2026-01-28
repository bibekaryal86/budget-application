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
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = channelHandlerContext.channel().attr(Constants.REQUEST_ID).get();
    QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
    String path = decoder.path();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.INSIGHTS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("[{}] Request: Method=[{}] Path=[{}]", requestId, method, path);

    // READ: GET /petssvc/api/v1/insights/cf-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CF_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CashFlowSummaryParams params = ServerUtils.getTransactionSummaryParams(decoder);
      handleCashFlowSummaries(requestId, channelHandlerContext, params);
      return;
    }

    // READ: GET /petssvc/api/v1/insights/cat-summaries
    if (path.equals(ApiPaths.INSIGHTS_V1_CAT_SUMMARIES) && method.equals(HttpMethod.GET)) {
      RequestParams.CategorySummaryParams params = ServerUtils.getCategorySummaryParams(decoder);
      handleCategorySummaries(requestId, channelHandlerContext, params);
      return;
    }

    log.info("[{}] Action Not Found: Method=[{}] Path=[{}]", requestId, method, path);
    channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
  }

  // READ TXN SUMMARIES
  private void handleCashFlowSummaries(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      RequestParams.CashFlowSummaryParams params)
      throws Exception {
    InsightsResponse.CashFlowSummaries response = service.readCashFLowSummaries(requestId, params);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ CAT SUMMARIES
  private void handleCategorySummaries(
      String requestId,
      ChannelHandlerContext channelHandlerContext,
      RequestParams.CategorySummaryParams params)
      throws Exception {
    InsightsResponse.CategorySummaries response = service.readCategoriesSummary(requestId, params);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
