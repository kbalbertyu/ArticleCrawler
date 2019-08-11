package org.albertyu.service;

import com.google.inject.Inject;
import org.albertyu.database.DBManager;
import org.albertyu.model.*;
import org.albertyu.source.Source;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.Exceptions.PastDateException;
import org.albertyu.utils.Tools;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:12
 */
public class CrawlExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CrawlExecutor.class);
    @Inject private WebDriverLauncher webDriverLauncher;
    @Inject private DBManager dbManager;
    @Inject Messengers messengers;

    public void execute(Config config) {
        WebDriver driver = null;
        try {
            driver = webDriverLauncher.start();
            for (Source source : config.getSources()) {
                List<Article> articles = source.fetchList(driver, config);

                int saved = 0;
                for (Article article : articles) {
                    String logId = Tools.toMD5(article.getUrl());
                    Article log = dbManager.readById(logId, Article.class);

                    if (log != null) {
                        logger.info("Article saved already: {} -> {}", article.getTitle(), article.getUrl());
                        continue;
                    }
                    try {
                        source.readArticle(driver, article);
                        this.saveArticle(driver, article, config);
                        dbManager.save(new ActionLog(logId), ActionLog.class);
                        saved++;
                    } catch (PastDateException e) {
                        logger.error("Article publish date has past {} minutes: {}",
                            config.getMaxPastMinutes(), article.getUrl(), e);
                    } catch (BusinessException e) {
                        String message = String.format("Unable to read/save article %s", article.getUrl());
                        logger.error(message, e);
                        Messenger messenger = new Messenger(this.getClass().getName(), message + ": " + e.getMessage());
                        this.messengers.add(messenger);
                    } catch (TimeoutException e) {
                        throw e;
                    } catch (Exception e) {
                        String message = String.format("Exception found in read/save article: %s -> %s", article.getTitle(), article.getUrl());
                        logger.error(message, e);

                        Messenger messenger = new Messenger(this.getClass().getName(), message + ": " + e.getMessage());
                        this.messengers.add(messenger);
                    }
                }

                Messenger messenger = new Messenger(this.getClass().getName(),
                    String.format("%d of %d articles are saved.", saved, articles.size()));
                this.messengers.add(messenger);
            }
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
    }

    private void saveArticle(WebDriver driver, Article article, Config config) {
        config.getHandler().saveArticle(article, driver, config);
    }
}
