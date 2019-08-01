package org.albertyu.application;

import org.albertyu.service.CrawlExecutor;
import org.albertyu.service.ExecutorInterface;
import org.albertyu.service.InstanceCaller;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/21 16:33
 */
public enum App {
    Website(InstanceCaller.getBean(CrawlExecutor.class)),
    API(InstanceCaller.getBean(CrawlExecutor.class));

    App(ExecutorInterface executor) {
        this.executor = executor;
    }

    public final ExecutorInterface executor;
}
