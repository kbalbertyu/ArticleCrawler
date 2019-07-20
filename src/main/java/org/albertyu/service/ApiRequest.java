package org.albertyu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.albertyu.model.ApiResult;
import org.albertyu.model.Config;
import org.albertyu.utils.Tools;
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
    private static final String WEB_API_ENDPOINT = "/api";
    private static final int MAX_REPEAT_TIMES = 3;

    public ApiResult get(String path, Config config) {
        return this.send(config, path, Method.GET, "");
    }

    public ApiResult post(String path, String dataText, Config config) {
        return this.send(config, path, Method.POST, dataText);
    }

    private static String getFullUrl(String path, Config config) {
        return config.getFrontUrl() + WEB_API_ENDPOINT + path;
    }

    private ApiResult send(Config config, String path, Method method, String dataText) {
        for (int i = 0; i < MAX_REPEAT_TIMES; i++) {
            try {
                String result = Jsoup.connect(getFullUrl(path, config)).ignoreContentType(true)
                    .data("data", dataText)
                    .method(method).timeout(60).maxBodySize(0).execute().body();
                ApiResult resultObj = JSON.parseObject(result, ApiResult.class);

                if (resultObj.getCode() == 1) {
                    return resultObj;
                }
                LOGGER.error("Request result failed: {} -> {}", path, resultObj.getMessage());
                break;
            } catch (IOException e) {
                LOGGER.error("Request result failed: {}", path, e);
                if (i < MAX_REPEAT_TIMES - 1) {
                    Tools.sleep(5);
                }
            } catch (JSONException e) {
                LOGGER.error("Invalid json response: {}", path);
            } catch (Exception e) {
                LOGGER.error("Unexpected exception occurred while requesting: {}", path, e);
                return null;
            }
        }
        return null;
    }
}
