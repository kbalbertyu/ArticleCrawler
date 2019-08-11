package org.albertyu.utils;

import org.albertyu.model.Constant;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.PageUtils.WaitTime;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 20:08
 */
class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    static HttpGet prepareGetRequest(String url) {
        HttpGet request = new HttpGet(url);
        request.addHeader("Connection", "keep-alive");
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        request.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        request.addHeader("Accept-Language", "en-US,en;q=0.8");
        request.addHeader("Cache-Control", "max-age=0");
        try {
            URL _url = new URL(url);
            request.addHeader("Host", _url.getHost());
            return request;
        } catch (MalformedURLException ex) {
            LOGGER.error("Error found in requesting content from url: {}", url);
            throw Lang.wrapThrow(ex);
        }
    }

    static Document getDocumentByJsoup(String url) {
        BusinessException exception = null;
        for (int i = 0; i < Constant.MAX_REPEAT_TIMES; i++) {
            try {
                return getDocument(url);
            } catch (Exception e) {
                exception = new BusinessException(e);
                LOGGER.error("Failed to load url of: {} -> {}", i + 1, url, e);
                if (i < Constant.MAX_REPEAT_TIMES - 1) {
                    WaitTime.Short.execute();
                }
            }
        }

        throw exception;
    }

    private static Document getDocument(String url) {
        Connection conn = connection(url);
        conn.timeout(Constant.DEFAULT_REQUEST_TIME_OUT);
        try {
            return conn.get();
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    private static Connection connection(String url) {
        Connection conn = Jsoup.connect(url);
        conn.header("Connection", "keep-alive");
        try {
            URL _url = new URL(url);
            conn.header("Host", _url.getHost());
        } catch (MalformedURLException e) {
            throw Lang.wrapThrow(e);
        }
        conn.header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
        conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.header("Accept-Language", "en-US,zh-CN;q=0.8,zh;q=0.5,en;q=0.3");
        conn.header("Accept-Encoding", "gzip, deflate");
        conn.header("Cache-Control", "max-age=0");
        conn.timeout(WaitTime.Long.valInMS());
        return conn;
    }
}
