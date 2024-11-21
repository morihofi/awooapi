package net.fuxle.awooapi.server.impl.netty;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.buffer.Unpooled;
import net.fuxle.awooapi.server.intf.Response;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyResponseWrapper extends Response {
    private final FullHttpResponse response;

    public NettyResponseWrapper() {
        this.response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.buffer());
    }

    @Override
    public void setHeader(String name, String value) {
        response.headers().set(name, value);
    }

    @Override
    public String getHeader(String name) {
        return response.headers().get(name);
    }

    @Override
    public void setBodyBytes(byte[] data) {
        response.content().writeBytes(data);
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headersMap = new HashMap<>();

        // Assuming response.headers() provides an Iterator<Map.Entry<String, String>>
        for (Map.Entry<String, String> entry : response.headers()) {
            String key = entry.getKey();
            String value = entry.getValue();
            headersMap.put(key, value);
        }

        return headersMap;
    }


    public FullHttpResponse getResponse() {
        return response;
    }
}