package org.albertyu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.albertyu.model.ApiResult;
import org.albertyu.utils.Tools;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Api request handler communicates with remote server
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:13
 */
public class ApiRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequest.class);
    private static final int MAX_REPEAT_TIMES = 3;

    public ApiResult get(String path) {
        return this.send(path, Method.GET, "");
    }

    public ApiResult post(String path, String dataText) {
        return this.send(path, Method.POST, dataText);
    }

    private ApiResult send(String url, Method method, String dataText) {
        for (int i = 0; i < MAX_REPEAT_TIMES; i++) {
            try {
                Connection conn = Jsoup.connect(url).ignoreContentType(true)
                    .method(method).timeout(60).maxBodySize(0);
                if (StringUtils.isNotBlank(dataText)) {
                    conn.data("data", dataText);
                }
                String result = conn.execute().body();
                ApiResult resultObj = JSON.parseObject(result, ApiResult.class);

                if (resultObj.getCode() == 1) {
                    return resultObj;
                }
                LOGGER.error("Request result failed: {} -> {}", url, resultObj.getMessage());
                break;
            } catch (IOException e) {
                LOGGER.error("Request result failed: {}", url, e);
                if (i < MAX_REPEAT_TIMES - 1) {
                    Tools.sleep(5);
                }
            } catch (JSONException e) {
                LOGGER.error("Invalid json response: {}", url);
            } catch (Exception e) {
                LOGGER.error("Unexpected exception occurred while requesting: {}", url, e);
                return null;
            }
        }
        return null;
    }
}
