package org.albertyu.service;

import com.google.inject.Inject;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:12
 */
public class CrawlExecutor {

    @Inject private WebDriverLauncher webDriverLauncher;

    public void execute() {
        WebDriver driver = webDriverLauncher.start();
        driver.close();
        driver.quit();
    }
}
