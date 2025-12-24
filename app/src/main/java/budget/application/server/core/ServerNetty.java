package budget.application.server.core;

import budget.application.common.Constants;
import budget.application.server.handlers.ExceptionHandler;
import budget.application.server.handlers.NotFoundHandler;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerNetty {

  final DataSource dataSource;

  public ServerNetty(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void start() throws Exception {
    final EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.BOSS_GROUP_THREADS);
    final EventLoopGroup workerGroup = new NioEventLoopGroup(Constants.WORKER_GROUP_THREADS);

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
                      .addLast(new ServerRouter(dataSource))
                      .addLast(new ExceptionHandler())
                      .addLast(new NotFoundHandler());
                }
              });

      final int serverPort =
          Integer.parseInt(
              CommonUtilities.getSystemEnvProperty(
                  Constants.ENV_SERVER_PORT, Constants.ENV_PORT_DEFAULT));
      final ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();

      log.info("Budget Server Started on Port [{}]...", serverPort);
      channelFuture.channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      log.info("Budget Server Stopped...");
    }
  }
}
