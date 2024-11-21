package net.fuxle.awooapi.server.impl.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import net.fuxle.awooapi.server.intf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyWebServer extends WebServer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NettyWebServer() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void start(int port) {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
                                    Request request = new NettyRequestWrapper(msg, ctx);
                                    NettyResponseWrapper nettyResponse = new NettyResponseWrapper();
                                    Response response = nettyResponse;

                                    Handler handler = getRouter().getHandler(request.getPath(), request.getMethod());
                                    if (handler != null) {
                                        try {
                                            handler.handle(new HandlerContext(request, response));
                                        } catch (Exception e) {
                                            log.error("Handler threw an unhandled exception", e);
                                            response.setStatus(500);
                                            response.setBody("Internal Server Error");
                                        }
                                    } else {
                                        response.setStatus(404);
                                        response.setBody("Not Found");
                                    }
                                    ctx.writeAndFlush(nettyResponse.getResponse());
                                }
                            });
                        }
                    });

            bootstrap.bind(port).sync();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Netty server", e);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
