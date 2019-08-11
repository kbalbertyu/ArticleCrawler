package org.albertyu.model;

import lombok.Data;
import org.albertyu.service.handler.AbstractHandler;
import org.albertyu.source.Source;
import org.albertyu.utils.Tools;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/20 18:32
 */
@Data
public class Config {
    public AbstractHandler handler;
    String baseUrl;
    String cdnUrl;

    /**
     * Admin login information
     */
    String username;
    String password;
    String loginPath;
    Map<String, String> cookies = new HashMap<>();

    String apiPath;
    String savePath;
    String fileUploadPath;

    Map<String, String> headers = new HashMap<>(); // Headers for API access

    Map<Category, Integer> categories = new HashMap<>();
    Source[] sources;
    String[] recipients;
    private int maxPastMinutes = 180;

    public String fullUrl(String url) {
        return Tools.getAbsoluteUrl(url, baseUrl);
    }

    public boolean canLogin() {
        return StringUtils.isNotBlank(loginPath)
            && StringUtils.isNotBlank(password)
            && StringUtils.isNotBlank(username);
    }
}