package org.albertyu.model;

import lombok.Data;
import org.nutz.dao.entity.annotation.*;

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
}
