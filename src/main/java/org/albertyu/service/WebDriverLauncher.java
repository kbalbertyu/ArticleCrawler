package org.albertyu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.albertyu.model.Config;
import org.albertyu.model.Enums.ChromeVersion;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.PageUtils;
import org.albertyu.utils.PageUtils.WaitTime;
import org.albertyu.utils.Tools;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:14
 */
class WebDriverLauncher {
    private static final String CHROME_DRIVER_KEY = "webdriver.chrome.driver";
    private static final String DOWNLOAD_PATH = System.getProperty("user.dir") + "/downloads";

    WebDriver start() {
        ChromeVersion chromeVersion = this.getLatestChromeVersion();
        if (chromeVersion == null || StringUtils.isBlank(chromeVersion.filePath())) {
            throw new BusinessException("Unable to detect Chrome version.");
        }
        System.setProperty(CHROME_DRIVER_KEY, chromeVersion.filePath());
        ChromeOptions options = this.prepareChromeOptions();
        return new ChromeDriver(options);
    }

    private ChromeVersion getLatestChromeVersion() {
        int chromeMajorVersion = Tools.getChromeMajorVersion();
        if (chromeMajorVersion == -1) {
            return null;
        }

        for (ChromeVersion cdv : ChromeVersion.values()) {
            int[] range = cdv.chromeVersionRange();
            if (chromeMajorVersion >= range[0] && chromeMajorVersion <= range[1]) {
                return cdv;
            }
        }
        return null;
    }

    private ChromeOptions prepareChromeOptions() {
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", DOWNLOAD_PATH);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        return options;
    }

    private void fetchAdminCookies(WebDriver driver, Config config) {
        driver.get(config.getLoginPath());

        File cookieFile = this.getCookieFile(config);
        if (!PageUtils.present(driver, By.id("mb_email"), WaitTime.Normal)) {
            this.loadCookies(driver, config, cookieFile);
            return;
        }

        if (cookieFile.exists()) {
            Map<String, String> cookies = JSON.parseObject(Tools.readFileToString(cookieFile), new TypeReference<Map<String, String>>() {
            });
            PageUtils.addCookies(driver, cookies);
            driver.get(config.getAdminUrl());
        }

        if (!PageLoadHelper.visible(driver, By.id("mb_email"), WaitTime.Normal)) {
            this.loadCookies(driver, config, cookieFile);
            return;
        }
        driver.manage().deleteAllCookies();

        PageUtils.setValue(driver, By.id("mb_email"), config.getAdminEmail());
        PageUtils.setValue(driver, By.id("login_mb_password"), config.getAdminPassword());
        PageUtils.click(driver, By.cssSelector("button[type=submit]"));
        WaitTime.Normal.execute();

        this.loadCookies(driver, config, cookieFile);
    }

    private void loadCookies(WebDriver driver, Config config, File cookieFile) {
        Map<String, String> adminCookie = PageUtils.getCookies(driver);
        adminCookies = new HashMap<>();
        adminCookies.put(config.getApplication(), adminCookie);
        Tools.writeStringToFile(cookieFile, JSON.toJSONString(adminCookie, true));
    }

    private File getCookieFile(Config config) {
        String cookieFileName = String.format("ASC-Cookies/%s.json", config.getApplication());
        return new File(Directory.Tmp.path(), cookieFileName);
    }
}
