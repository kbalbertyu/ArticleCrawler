package org.albertyu.service.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.inject.Inject;
import org.albertyu.model.*;
import org.albertyu.utils.DownloadUtils;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.PageUtils;
import org.albertyu.utils.PageUtils.WaitTime;
import org.albertyu.utils.RegexUtils;
import org.albertyu.utils.Tools;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Access to server from website admin portal with admin cookies
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:04
 */
public class Handler1 extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(Handler1.class);
    @Inject Messengers messengers;
    private Map<String, String> cookies = new HashMap<>();

    /**
     * Download the images from article source,
     * then upload to server temp directory
     */
    ImageUploadResult uploadImages(Article article, WebDriver driver, Config config) {
        if (!article.hasImages()) {
            return null;
        }
        Connection conn = this.createWebConnection(config.getFileUploadPath(), null);

        int i = 0;
        for (String imageUrl : article.getContentImages()) {
            DownloadResult downloadResult;
            try {
                downloadResult = DownloadUtils.downloadFile(imageUrl, driver);
            } catch (BusinessException e) {
                String message = String.format("Unable to download image: %s", imageUrl);
                logger.error(message);
                Messenger messenger = new Messenger(this.getClass().getName(), message + ": " + e.getMessage());
                this.messengers.add(messenger);
                continue;
            }
            String image = downloadResult.getFullPath();
            File file = FileUtils.getFile(image);
            try {
                FileInputStream fs = new FileInputStream(file);
                conn.data("files[" + i + "]", image, fs);
                i++;
            } catch (IOException e) {
                String message = String.format("Unable to download file: %s", image);
                logger.error(message, e);
                Messenger messenger = new Messenger(this.getClass().getName(), message + ": " + e.getMessage());
                this.messengers.add(messenger);
            }
        }
        if (i == 0) {
            logger.error("No files downloaded.");
            return null;
        }
        String message = null;
        for (int j = 0; j < Constant.MAX_REPEAT_TIMES; j++) {
            try {
                String body = conn.execute().body();
                ImageUploadResult result = JSONObject.parseObject(body, ImageUploadResult.class);
                if (!result.hasFiles()) {
                    message = String.format("Files are not uploaded, retry uploading: %s", body);
                    logger.error(message);
                    continue;
                }
                return result;
            } catch (Exception e) {
                message = String.format("Unable to upload files, retry in %d seconds:", WaitTime.Normal.val());
                logger.error(message, e);
                WaitTime.Normal.execute();
            }
        }
        Messenger messenger = new Messenger(this.getClass().getName(), String.format("Unable to upload file: %s", message));
        this.messengers.add(messenger);
        return null;
    }

    @Override
    void uploadArticle(Article article, WebDriver driver, Config config) {
        Connection conn = this.createWebConnection(config.getSavePath(), cookies)
            .data("title", article.getTitle())
            .data("summary", article.getSummary())
            .data("content", article.getContent())
            .data("category", String.valueOf(article.getCategory()))
            .data("timestamp", String.valueOf(article.getTimestamp()));
        if (article.hasImageIds()) {
            StringBuilder sb = new StringBuilder();

            int length = article.getImageIds().length;
            for (int i = 0; i < length; i++) {
                String imageId = String.valueOf(article.getImageIds()[i]);
                conn.data("imghidden_" + imageId, "")
                    .data("ar_image_hide[" + i + "]", imageId);
                sb.append(imageId);
                if (i != length - 1) {
                    sb.append(":");
                }
            }
            conn.data("ar_image", sb.toString());
        }
        for (int i = 0; i < Constant.MAX_REPEAT_TIMES; i++) {
            try {
                String articleId = conn.execute().body();
                if (RegexUtils.match(articleId, "\\d+")) {
                    logger.info("Article saved: {} -> {}", article.getTitle(), articleId);
                    return;
                } else {
                    String message = String.format("Article saved failed: %s", article.getTitle());
                    logger.error(message);
                    Messenger messenger = new Messenger(this.getClass().getName(), message);
                    this.messengers.add(messenger);
                }
            } catch (IOException e) {
                String message = "Unable to save the article:";
                logger.error(message, e);
                Messenger messenger = new Messenger(this.getClass().getName(), message + e.getMessage());
                this.messengers.add(messenger);
                WaitTime.Normal.execute();
            }
        }
        throw new BusinessException(String.format("Unable to save the article: [%s]%s -> %s",
            article.getSource(), article.getTitle(), article.getUrl()));
    }

    /**
     * Save uploaded images to DB, and move out of the temp directory
     */
    List<SavedImage> saveImages(Article article, ImageUploadResult result, Config config) {
        Connection conn = this.createWebConnection(config.getFileUploadPath(), cookies);

        int i = 0;
        for (UploadedImage imageFile : result.getFiles()) {
            conn.data("im_title[" + i + "]", article.getTitle())
                .data("im_content[" + i + "]", "")
                .data("im_credit[" + i + "]", article.getSource())
                .data("im_link[" + i + "]", article.getUrl())
                .data("im_x_pos[" + i + "]", "50")
                .data("im_y_pos[" + i + "]", "40")
                .data("uploadedFile[" + i + "]", imageFile.getName())
                .data("originalFile[" + i + "]", imageFile.getOriginalFile());
            i++;
        }

        for (int j = 0; j < Constant.MAX_REPEAT_TIMES; j++) {
            try {
                String body = conn.execute().body();
                return JSONObject.parseArray(body, SavedImage.class);
            } catch (IOException e) {
                String message = String.format("Unable to save the files, retry in %d seconds:", WaitTime.Normal.val());
                logger.error(message, e);
                Messenger messenger = new Messenger(this.getClass().getName(), message + ": " + e.getMessage());
                this.messengers.add(messenger);
                WaitTime.Normal.execute();
            }
        }
        return null;
    }

    /**
     * Create web connection to admin site
     */
    private Connection createWebConnection(String url, Map<String, String> cookies) {
        Connection conn = Jsoup.connect(url)
            .userAgent("Mozilla")
            .method(Method.POST)
            .data("maxFileNum", "50")
            .data("maxFileSize", "20 MB")
            .data("unique_key", Tools.toMD5(String.valueOf(System.currentTimeMillis())))
            .data("field", "image_hidden")
            .data("func", "photo_image_content")
            .data("request_from", "ContentCrawler");
        if (cookies != null) {
            conn.cookies(cookies);
        }
        return conn;
    }

    @Override
    public void access(WebDriver driver, Config config) {
        if (!config.canLogin()) {
            return;
        }
        this.fetchAdminCookies(driver, config);
    }

    private void fetchAdminCookies(WebDriver driver, Config config) {
        driver.get(config.getLoginPath());
        File cookieFile = this.getCookieFile(config);
        By usernameBy = By.id("username");

        // If login already, save the cookies
        if (!PageUtils.present(driver, usernameBy, WaitTime.Normal)) {
            this.saveCookies(driver, cookieFile);
            return;
        }

        // If not login yet, try loading cookies from file
        if (cookieFile.exists()) {
            Set<Cookie> cookies = this.getCookies(cookieFile);
            PageUtils.addCookies(driver, cookies);
            driver.get(config.getLoginPath());
        }

        // If login already after loading cookies from file
        if (!PageUtils.present(driver, usernameBy, WaitTime.Normal)) {
            this.saveCookies(driver, cookieFile);
            return;
        }

        // If not login yet after trying above process, perform logging in
        driver.manage().deleteAllCookies();
        PageUtils.setValue(driver, usernameBy, config.getUsername());
        PageUtils.setValue(driver, By.id("password"), config.getPassword());
        PageUtils.submit(driver, By.id("submit"));
        WaitTime.Normal.execute();
        if (!PageUtils.present(driver, usernameBy, WaitTime.Normal)) {
            this.saveCookies(driver, cookieFile);
            return;
        }

        throw new BusinessException(String.format("Unable to fetch admin cookies from: %s", config.getLoginPath()));
    }

    private Set<Cookie> getCookies(File cookieFile) {
        return JSON.parseObject(Tools.readFileToString(cookieFile), new TypeReference<Set<Cookie>>() {
                });
    }

    private void saveCookies(WebDriver driver, File file) {
        Set<Cookie> cookies = driver.manage().getCookies();

        for (Cookie cookie : cookies) {
            this.cookies.put(cookie.getName(), cookie.getValue());
        }
        try {
            FileUtils.writeStringToFile(file, JSON.toJSONString(cookies), Constant.UTF8);
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    private File getCookieFile(Config config) {
        try {
            URL url = new URL(config.getLoginPath());
            String cookieFileName = String.format("cookies/%s.json", url.getHost());
            return new File(Constant.TMP_PATH, cookieFileName);
        } catch (MalformedURLException e) {
            String message = String.format("Unable to extract host from url: %s", config.getLoginPath());
            logger.error(message, e);
            throw new BusinessException(message);
        }
    }
}
