package org.albertyu.model;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:20
 */
@Data
public class ImageUploadResult {
    private List<UploadedImage> files;

    private void filterErrorFiles() {
        files.removeIf(file -> StringUtils.isNotBlank(file.getError()));
    }

    public boolean hasFiles() {
        this.filterErrorFiles();
        return !CollectionUtils.isEmpty(files);
    }
}
