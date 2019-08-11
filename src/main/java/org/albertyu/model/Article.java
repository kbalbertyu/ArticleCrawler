package org.albertyu.model;

import lombok.Data;
import org.albertyu.utils.Exceptions.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.entity.annotation.*;

import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/20 15:08
 */
@Table("article")
@Data
public class Article implements TableInterface {

    @Id
    private int id;

    @Name
    private String name;

    @Column
    private Category category;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String url;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String title;

    @Column
    @ColDefine(type = ColType.TEXT)
    private String summary;

    @Column
    @ColDefine(type = ColType.TEXT)
    private String content;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 25)
    private String source; // Third party content source

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 25)
    private String application; // Target website

    @Column
    private long publishDate;

    @Column
    private long timestamp;

    private int categoryId;
    private List<String> contentImages;

    public boolean hasImages() {
        return CollectionUtils.isNotEmpty(contentImages);
    }

    public void validate() {
        if (StringUtils.isBlank(title)) {
            throw new BusinessException(String.format("Article title is blank: %s", url));
        }
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(String.format("Article content is blank: %s -> %s", title, url));
        }
    }

    @Override
    public String getPK() {
        return url;
    }

    private int[] imageIds;

    public boolean hasImageIds() {
        return ArrayUtils.isNotEmpty(imageIds);
    }
}
