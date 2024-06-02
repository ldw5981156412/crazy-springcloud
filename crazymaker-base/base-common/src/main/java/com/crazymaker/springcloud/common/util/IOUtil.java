package com.crazymaker.springcloud.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class IOUtil {

    public static void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读jar包根目录下的文件
     * @param loader
     * @param resourceName
     * @return
     */
    public static String loadJarFile(ClassLoader loader, String resourceName) {
        InputStream in = loader.getResourceAsStream(resourceName);
        if (in == null) {
            return null;
        }
        String out = null;
        try {
            int len = in.available();
            byte[] data = new byte[len];
            int readLength = in.read(data);
            if ((long) readLength < len) {
                throw new IOException(String.format("File length is [{}] but read [{}]!", len, readLength));
            }
            out = new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(in);
        }
        return out;
    }
}
