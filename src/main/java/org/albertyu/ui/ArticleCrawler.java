package org.albertyu.ui;

import org.albertyu.service.CrawlExecutor;
import org.albertyu.service.InstanceCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:11
 */
public class ArticleCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlExecutor.class);

    public static void main(String[] args) {
        LOGGER.info("Start Crawl Executor.");
        InstanceCaller.getBean(CrawlExecutor.class).execute();
        LOGGER.info("Crawl Executor job completed.");
        System.exit(0);
    }
}
