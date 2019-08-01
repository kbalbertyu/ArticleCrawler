package org.albertyu.utils;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/1 19:51
 */
public class Exceptions {

    public static class BusinessException extends RuntimeException {
        private static final long serialVersionUID = 1234564065240518738L;

        public BusinessException(String errorMsg) {
            super(errorMsg);
        }

        public BusinessException(Throwable cause) {
            super(cause);
        }
    }
}
