package org.albertyu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019-01-10 8:21 PM
 */
@Data
@AllArgsConstructor
public class DownloadResult {
    private String url; // Original url from crawled content
    private String fullPath; // Full source url of resource
    private String filePath; // Download file path
}
