package budget.application.server.utils;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ServerUtils {
  public static void sendResponse(
      final FullHttpResponse fullHttpResponse, final ChannelHandlerContext channelHandlerContext) {
    channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
  }

  public static void sendResponse(
      ChannelHandlerContext channelHandlerContext, HttpResponseStatus status, String errMsg) {
    ResponseMetadata.ResponseStatusInfo responseStatusInfo;

    if (CommonUtilities.isEmpty(errMsg)) {
      responseStatusInfo = ResponseMetadata.emptyResponseStatusInfo();
    } else {
      responseStatusInfo = new ResponseMetadata.ResponseStatusInfo(errMsg);
    }

    ResponseMetadata responseMetadata =
        new ResponseMetadata(
            responseStatusInfo,
            ResponseMetadata.emptyResponseCrudInfo(),
            ResponseMetadata.emptyResponsePageInfo());

    byte[] jsonResponse =
        CommonUtilities.writeValueAsBytesNoEx(new ResponseWithMetadata(responseMetadata));
    FullHttpResponse fullHttpResponse =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(jsonResponse));
    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, jsonResponse.length);
    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);

    channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
  }
}
