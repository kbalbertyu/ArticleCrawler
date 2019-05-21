package org.albertyu.service;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/22 3:53
 */
public class InstanceCaller {

    private static final Injector injector = Guice.createInjector();

    public static <T> T getBean(Class<T> type) {
        return injector.getInstance(type);
    }
}
