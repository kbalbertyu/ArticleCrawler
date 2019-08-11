package org.albertyu.model;

import lombok.Data;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:21
 */
@Data
public class UploadedImage {
    private String originalFile;
    private String name;
    private String size;
    private String error;
}
