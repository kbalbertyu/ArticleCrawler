package org.albertyu.source;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 16:57
 */
public abstract class SourceWithoutDriver extends Source {

    @Override
    boolean withoutDriver() {
        return true;
    }
}
