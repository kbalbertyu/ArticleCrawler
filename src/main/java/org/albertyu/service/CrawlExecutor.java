package org.albertyu.service;

import com.google.inject.Inject;
import org.albertyu.model.config.Config;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:12
 */
public class CrawlExecutor implements ExecutorInterface {

    @Inject private WebDriverLauncher webDriverLauncher;

    public void execute(Config config) {
        WebDriver driver = webDriverLauncher.start();
        driver.close();
        driver.quit();
    }
}
