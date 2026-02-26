package budget.application.server.handlers;

import budget.application.common.Constants;
import budget.application.model.dto.AccountRequest;
import budget.application.model.dto.AccountResponse;
import budget.application.server.util.ApiPaths;
import budget.application.server.util.ServerUtils;
import budget.application.service.domain.AccountService;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger log = LoggerFactory.getLogger(AccountHandler.class);

  private final AccountService accountService;

  public AccountHandler(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  protected void channelRead0(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String path = fullHttpRequest.uri();
    HttpMethod method = fullHttpRequest.method();

    if (!path.startsWith(ApiPaths.ACCOUNTS_V1)) {
      channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
      return;
    }
    log.info("Request: Method=[{}] Path=[{}]", method, path);

    // CREATE: POST /petssvc/api/v1/accounts
    if (path.equals(ApiPaths.ACCOUNTS_V1) && method.equals(HttpMethod.POST)) {
      handleCreate(channelHandlerContext, fullHttpRequest);
      return;
    }

    // READ ALL: GET /petssvc/api/v1/accounts
    if (path.equals(ApiPaths.ACCOUNTS_V1) && method.equals(HttpMethod.GET)) {
      handleReadAll(channelHandlerContext);
      return;
    }

    // READ TYPES: GET /petssvc/api/v1/accounts/types
    if (path.equals(ApiPaths.ACCOUNTS_V1_TYPES) && method.equals(HttpMethod.GET)) {
      handleReadAccountTypes(channelHandlerContext);
      return;
    }

    // READ STATUSES: GET /petssvc/api/v1/accounts/statuses
    if (path.equals(ApiPaths.ACCOUNTS_V1_STATUSES) && method.equals(HttpMethod.GET)) {
      handleReadAccountStatuses(channelHandlerContext);
      return;
    }

    // READ BANKS: GET /petssvc/api/v1/accounts/banks
    if (path.equals(ApiPaths.ACCOUNTS_V1_BANKS) && method.equals(HttpMethod.GET)) {
      handleReadAccountBanks(channelHandlerContext);
      return;
    }

    // READ ONE: GET /petssvc/api/v1/accounts/{id}
    if (path.startsWith(ApiPaths.ACCOUNTS_V1_WITH_ID) && method.equals(HttpMethod.GET)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.ACCOUNTS_V1_WITH_ID);
      handleReadOne(channelHandlerContext, id);
      return;
    }

    // UPDATE: PUT /petssvc/api/v1/accounts/{id}
    if (path.startsWith(ApiPaths.ACCOUNTS_V1_WITH_ID) && method.equals(HttpMethod.PUT)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.ACCOUNTS_V1_WITH_ID);
      handleUpdate(channelHandlerContext, fullHttpRequest, id);
      return;
    }

    // DELETE: DELETE /petssvc/api/v1/accounts/{id}
    if (path.startsWith(ApiPaths.ACCOUNTS_V1_WITH_ID) && method.equals(HttpMethod.DELETE)) {
      UUID id = ServerUtils.getEntityId(path, ApiPaths.ACCOUNTS_V1_WITH_ID);
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
    AccountRequest request = ServerUtils.getRequestBody(fullHttpRequest, AccountRequest.class);
    AccountResponse response = accountService.create(request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.CREATED, response);
  }

  // READ ALL
  private void handleReadAll(ChannelHandlerContext channelHandlerContext) throws Exception {
    AccountResponse response = accountService.read(List.of());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ACCOUNT TYPES
  private void handleReadAccountTypes(ChannelHandlerContext channelHandlerContext)
      throws Exception {
    AccountResponse.AccountRefLists response =
        new AccountResponse.AccountRefLists(
            Constants.ACCOUNT_TYPES, ResponseMetadata.emptyResponseMetadata());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ACCOUNT STATUSES
  private void handleReadAccountStatuses(ChannelHandlerContext channelHandlerContext)
      throws Exception {
    AccountResponse.AccountRefLists response =
        new AccountResponse.AccountRefLists(
            Constants.ACCOUNT_STATUSES, ResponseMetadata.emptyResponseMetadata());
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ BANKS
  private void handleReadAccountBanks(ChannelHandlerContext channelHandlerContext)
      throws Exception {
    AccountResponse.AccountRefLists response = accountService.readAccountBanks();
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // READ ONE
  private void handleReadOne(ChannelHandlerContext channelHandlerContext, UUID id)
      throws Exception {
    AccountResponse response = accountService.read(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // UPDATE
  private void handleUpdate(
      ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, UUID id)
      throws Exception {
    AccountRequest request = ServerUtils.getRequestBody(fullHttpRequest, AccountRequest.class);
    AccountResponse response = accountService.update(id, request);
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }

  // DELETE
  private void handleDelete(ChannelHandlerContext channelHandlerContext, UUID id) throws Exception {
    AccountResponse response = accountService.delete(List.of(id));
    ServerUtils.sendResponse(channelHandlerContext, HttpResponseStatus.OK, response);
  }
}
