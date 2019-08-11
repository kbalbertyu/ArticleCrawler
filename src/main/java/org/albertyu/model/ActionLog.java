package org.albertyu.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 14:47
 */
@Table("action_log")
@AllArgsConstructor
@NoArgsConstructor
public class ActionLog implements TableInterface {

    public ActionLog(String id) {
        this(id, new Date().getTime());
    }

    @Override
    public String toString() {
        return id + Constant.DASH + timestamp;
    }

    @Getter @Setter @Name private String id;
    @Getter @Setter @Column private long timestamp;

    @Override
    public String getPK() {
        return id;
    }
}
