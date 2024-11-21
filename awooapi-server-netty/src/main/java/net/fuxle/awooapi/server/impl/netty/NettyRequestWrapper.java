package net.fuxle.awooapi.server.impl.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.fuxle.awooapi.server.intf.Request;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyRequestWrapper implements Request {
    private final FullHttpRequest request;
    private final ChannelHandlerContext ctx;

    public NettyRequestWrapper(FullHttpRequest request, ChannelHandlerContext ctx) {
        this.request = request;
        this.ctx = ctx;
    }

    @Override
    public String getPath() {
        return request.uri();
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public String getBody() {
        return request.content().toString(io.netty.util.CharsetUtil.UTF_8);
    }

    @Override
    public String getIP() {
        return getRemoteAddr();
    }

    private String getRemoteAddr() {
        String remoteAddress = request.headers().get("X-Forwarded-For");
        if (remoteAddress == null || remoteAddress.isEmpty()) {
            SocketAddress socketAddress = ctx.channel().remoteAddress();
            if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
                return inetSocketAddress.getAddress().getHostAddress();
            }
            return socketAddress.toString();
        }
        return remoteAddress;
    }
}