package org.albertyu.ui;

import org.albertyu.model.Config;
import org.albertyu.service.CrawlExecutor;
import org.albertyu.service.InstanceCaller;
import org.albertyu.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:11
 */
public class ArticleCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleCrawler.class);

    public static void main(String[] args) {
        LOGGER.info("Start crawling contents.");

        Config config = Tools.loadAppConfig();
        try {
            LOGGER.info("Running application");
            InstanceCaller.getBean(CrawlExecutor.class).execute(config);
        } catch (Exception e) {
            LOGGER.error("Unknown error found: ", e);
        } finally {
            System.exit(0);
        }
    }
}
