package com.pro_crafting.tools.jsonpretty;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import java.io.OutputStream;

public class HttpServerResponseOutputStream extends OutputStream {

    private final HttpServerResponse response;

    public HttpServerResponseOutputStream(HttpServerResponse response) {
        this.response = response;
    }

    @Override
    public void write(int b) {
        response.write(Buffer.buffer(new byte[]{(byte) b}));
    }

    @Override
    public void write(byte[] b) {
        response.write(Buffer.buffer(b));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        response.write(Buffer.buffer(b));
    }
}
