package org.albertyu.utils;

import org.albertyu.model.Constant;
import org.albertyu.model.DownloadResult;
import org.albertyu.model.ImageType;
import org.albertyu.utils.Exceptions.BusinessException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 20:18
 */
public class DownloadUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageUtils.class);

    private static File makeDownloadFile(String fileName) {
        File file = new File(Constant.DOWNLOAD_PATH, fileName);
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
            file = new File(Constant.DOWNLOAD_PATH, fileName);
        }
        if (!file.canWrite()) {
            file.setWritable(true);
        }
        return file;
    }

    public static DownloadResult downloadFile(String url, WebDriver driver) {
        String originalUrl = url;
        url = Tools.getAbsoluteUrl(url, driver.getCurrentUrl());
        String fileName = Tools.extractFileNameFromUrl(url);
        File file = makeDownloadFile(fileName);

        HttpGet get = HttpUtils.prepareGetRequest(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, PageUtils.getCookieStore(driver));

        for (int i = 0; i < Constant.MAX_REPEAT_TIMES; i++) {
            CloseableHttpResponse resp = null;
            InputStream is = null;
            try {
                resp = httpClient.execute(get, localContext);
                int status = resp.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    is = resp.getEntity().getContent();
                    FileUtils.copyInputStreamToFile(is, file);
                    String path = file.getAbsolutePath();

                    DownloadResult result = makeDownloadResult(url, originalUrl, path);
                    LOGGER.info("{} file downloaded, Size:{}, path:{}",
                        result.getFilePath(),
                        FileUtils.byteCountToDisplaySize(file.length()), result.getFullPath());
                    return result;
                }
                String message = String.format("Failed to execute file download request: filePath=%s, url=%s, status=%s.", fileName, originalUrl, status);
                LOGGER.error(message);
            } catch (Exception ex) {
                String message = String.format("Failed to download file of %s： %s", fileName, ex.getMessage());
                LOGGER.error(message);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (resp != null) {
                        resp.close();
                    }
                } catch (IOException e) {
                    LOGGER.error("Unable to close the input stream:", e);
                }
                get.releaseConnection();
                file.deleteOnExit();
            }
        }
        throw new BusinessException(String.format("Failed to execute %s file download request after retried.", fileName));
    }

    /**
     * Generate new name with MD5 to avoid duplicated image names in an article
     */
    private static DownloadResult makeDownloadResult(String url, String originalUrl, String path) throws IOException {
        ImageType type = Tools.determineImageFileType(path);
        if (!type.allowed()) {
            type = ImageType.DEFAULT_TYPE;
            Tools.convertImageFileType(url, path, type);
        }
        String fileNameNew = Tools.toMD5(originalUrl) + "." + type.toExt();

        File fileNew = makeDownloadFile(fileNameNew);
        FileUtils.moveFile(FileUtils.getFile(path), fileNew);
        path = fileNew.getAbsolutePath();

        return new DownloadResult(originalUrl, path, fileNameNew);
    }
}
