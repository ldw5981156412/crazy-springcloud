package com.crazymaker.springcloud.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpRequestUtil {
    // 发送请求的客户端单例
    private static CloseableHttpClient httpClient;

    public static String simpleGet(String url) throws IOException {
        // 1 直接创建客户端
        CloseableHttpClient client = HttpClientBuilder.create().build();
        //2 创建请求
        HttpGet httpGet = new HttpGet(url);
        //3 超时配置
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000)
                .setConnectTimeout(1000)
                .build();
        httpGet.setConfig(requestConfig);
        //4 发送请求，处理响应
        return simpleRequestData(url, client, httpGet);
    }

    /**
     * 内部的请求发送
     *
     * @param url     连接地址
     * @param client  客户端
     * @param request post 或者 getStr 或者其他请求
     * @return 请求字符串
     */
    private static String simpleRequestData(String url, CloseableHttpClient client, HttpRequest request) throws IOException {
        CloseableHttpResponse response = null;
        InputStream in = null;
        String result = null;
        try {
            HttpHost httpHost = getHost(url);
            response = client.execute(httpHost, request, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                result = IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        } finally {
            quietlyClose(in);
            quietlyClose(response);
            //释放HttpClient 连接。
            quietlyClose(client);
        }

        return result;
    }

    /**
     * 从url 中获取主机
     *
     * @param url url 地址
     * @return HttpHost
     */
    private static HttpHost getHost(String url) {
        String hostName = url.split("/")[2];
        int port = 80;
        if (hostName.contains(":")) {
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        return new HttpHost(hostName, port);
    }

    /**
     * 安静的关闭可关闭对象
     *
     * @param closeable 可关闭对象
     */
    private static void quietlyClose(java.io.Closeable closeable) {
        if (null == closeable) return;
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
