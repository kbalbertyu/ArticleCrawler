package org.albertyu.model;

import lombok.Data;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:22
 */
@Data
public class SavedImage {
    private String originalFile;
    private String uploadedFile;
    private int imageId;
    private String fileName;
    private String path;
}
