package org.albertyu.service;

import org.albertyu.model.Enums.ChromeVersion;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.Tools;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.HashMap;

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
}
