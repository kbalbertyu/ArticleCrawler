package org.albertyu.ui;

import org.albertyu.service.CrawlExecutor;
import org.albertyu.service.InstanceCaller;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/13 3:11
 */
public class ArticleCrawler {

    public static void main(String[] args) {
        InstanceCaller.getBean(CrawlExecutor.class).execute();
    }
}
