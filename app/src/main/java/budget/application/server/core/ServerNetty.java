package budget.application.server.core;

import budget.application.common.Constants;
import budget.application.server.handlers.ExceptionHandler;
import budget.application.server.handlers.NotFoundHandler;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerNetty {
  private static final Logger log = LoggerFactory.getLogger(ServerNetty.class);

  private final ServerContext serverContext;
  private Channel channel;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  public ServerNetty(ServerContext serverContext) {
    this.serverContext = serverContext;
  }

  public void start() throws Exception {
    bossGroup =
        new MultiThreadIoEventLoopGroup(Constants.BOSS_GROUP_THREADS, NioIoHandler.newFactory());
    workerGroup =
        new MultiThreadIoEventLoopGroup(Constants.WORKER_GROUP_THREADS, NioIoHandler.newFactory());

    try {
      final ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constants.CONNECT_TIMEOUT_MILLIS)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(final SocketChannel socketChannel) throws Exception {
                  socketChannel
                      .pipeline()
                      .addLast(new HttpServerCodec())
                      .addLast(new HttpObjectAggregator(Constants.MAX_CONTENT_LENGTH))
                      .addLast(new ServerLogging())
                      .addLast(new ServerSecurity())
                      .addLast(new ServerRouter(serverContext))
                      .addLast(new ExceptionHandler())
                      .addLast(new NotFoundHandler());
                }
              });

      final int serverPort =
          Integer.parseInt(
              CommonUtilities.getSystemEnvProperty(
                  Constants.ENV_SERVER_PORT, Constants.ENV_PORT_DEFAULT));
      final ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();
      channel = channelFuture.channel();
      log.info("Budget Server Started on Port [{}]...", getBoundPort());
      if (!isTestMode()) {
        channel.closeFuture().sync();
      }
    } catch (Exception ex) {
      log.error("Budget Server Start Exception", ex);
      throw new RuntimeException(ex);
    }
  }

  public void stop() {
    try {
      if (channel != null) {
        channel.close().sync();
      }
    } catch (InterruptedException ignored) {
    }
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
    log.info("Budget Server Stopped...");
  }

  public int getBoundPort() {
    if (channel == null) {
      throw new IllegalStateException("Server not started yet...");
    }
    return ((InetSocketAddress) channel.localAddress()).getPort();
  }

  private boolean isTestMode() {
    return Constants.TESTING_ENV.equals(
        CommonUtilities.getSystemEnvProperty(Constants.SPRING_PROFILES_ACTIVE));
  }
}
