package com.lesofn.archforge.starter.requestlog;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;

/**
 * 请求体 eager 缓存包装：构造时读尽 body，下游可重复读取。
 * 若上游已有可重读包装（如 RepeatableFilter），本包装只是再缓存一层，零行为差异。
 */
public class RequestLogRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public RequestLogRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /** 已缓存的请求体原文。 */
    public byte[] getBody() { return body; }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return bais.read(b, off, len);
            }

            @Override
            public boolean isFinished() { return bais.available() == 0; }

            @Override
            public boolean isReady() { return true; }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 非异步请求，无需实现
            }
        };
    }
}
