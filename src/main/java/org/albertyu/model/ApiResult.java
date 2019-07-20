package org.albertyu.model;

import lombok.Data;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/22 4:06
 */
@Data
public class ApiResult {
    private int code;
    private String message;
    private String data;
}
