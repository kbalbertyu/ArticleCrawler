package org.albertyu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 7:04
 */
@Data
@AllArgsConstructor
public class CSSQuery {
    private String list;
    private String title;
    private String time;
    private String summary;
    private String content;
}
