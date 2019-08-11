package org.albertyu.utils;

import org.albertyu.model.Article;
import org.albertyu.model.Constant;
import org.albertyu.utils.Exceptions.BusinessException;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 19:53
 */
public class PageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageUtils.class);
    private static final long SIMPLE_WAIT_MS = 500;

    public enum WaitTime {
        Long(20),
        Normal(5),
        Short(3),
        Shortest(1);

        private final int value;

        public int val() {
            return value;
        }

        public int valInMS() {
            return value * 1000;
        }

        public void execute() {
            Tools.sleep(this.valInMS());
        }

        /**
         * @param value time in second
         */
        WaitTime(int value) {
            this.value = value;
        }
    }

    public static Document openArticlePage(WebDriver driver, Article article, boolean withoutDriver) {
        return openPage(driver, article.getUrl(), withoutDriver);
    }

    public static Document openPage(WebDriver driver, String url, boolean withoutDriver) {
        if (withoutDriver) {
            try {
                return HttpUtils.getDocumentByJsoup(url);
            } catch (BusinessException e) {
                LOGGER.error("Unable to load page via Jsoup, try using WebDriver: {}", url);
            }
        }
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            LOGGER.warn("List page loading timeout, try ignoring the exception: {}", url);
        }

        // Scroll to bottom to make sure latest content are loaded
        PageUtils.scrollToBottom(driver);
        WaitTime.Normal.execute();

        return Jsoup.parse(driver.getPageSource());
    }

    static CookieStore getCookieStore(WebDriver driver) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        for (Cookie seleniumCookie : driver.manage().getCookies()) {
            BasicClientCookie cookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            cookie.setDomain(seleniumCookie.getDomain());
            cookie.setSecure(seleniumCookie.isSecure());
            cookie.setExpiryDate(seleniumCookie.getExpiry());
            cookie.setPath(seleniumCookie.getPath());
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }

    public static void scrollToBottom(WebDriver driver) {
        for (int j = 0; j < Constant.MAX_REPEAT_TIMES; j++) {
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            WaitTime.Normal.execute();
        }
    }

    public static void removeElementByClass(WebDriver driver, String className) {
        try {
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("var elem = document.getElementsByClassName('" + className + "')[0];"
                + "elem.parentNode.removeChild(elem);");
        } catch (WebDriverException e) {
            LOGGER.error("Unable to remove element by class: {}", className);
        }
    }

    public static void loadLazyContent(WebDriver driver) {
        scrollToBottom(driver);
        scrollToTop(driver);
        long height = getPageHeight(driver);

        long to = 0L;
        long by = 300L;
        while (Long.compare(to, height) == -1) {
            scrollBy(driver, by);
            height = getPageHeight(driver);
            to += by;
        }
    }

    private static long getPageHeight(WebDriver driver) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        return (Long) jse.executeScript("return document.body.scrollHeight;");
    }

    private static void scrollToTop(WebDriver driver) {
        for (int j = 0; j < Constant.MAX_REPEAT_TIMES; j++) {
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("window.scrollTo(0, 0);");
            Tools.sleep(SIMPLE_WAIT_MS);
        }
    }

    private static void scrollBy(WebDriver driver, long by) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("window.scrollBy(0, " + by + ");");
        WaitTime.Shortest.execute();
    }

    public static boolean present(WebDriver driver, By by, WaitTime waitTime) {
        try {
            return (new WebDriverWait(driver, waitTime.val())).until(ExpectedConditions.presenceOfElementLocated(by)) != null;
        } catch (WebDriverException e) {
            return false;
        }
    }
}
