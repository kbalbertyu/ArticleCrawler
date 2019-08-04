package org.albertyu.model.config;

import lombok.Data;
import org.albertyu.application.App;
import org.albertyu.model.Category;
import org.albertyu.source.Source;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/20 18:32
 */
@Data
public class Config {
    App app;
    String baseUrl;

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
}