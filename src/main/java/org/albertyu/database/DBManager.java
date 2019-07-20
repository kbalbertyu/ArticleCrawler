package org.albertyu.database;

import com.google.inject.Inject;
import org.albertyu.model.Article;
import org.albertyu.model.TableInterface;
import org.nutz.dao.impl.NutDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/20 15:25
 */
public class DBManager {

    private final static Map<String, Class<? extends TableInterface>> TABLES = new HashMap<>();
    static {
        TABLES.put("article", Article.class);
    }

    private NutDao dao;

    @Inject
    public void initDataSource() {
        Ioc ioc = new NutIoc(new JsonLoader("conf/jdbc.json"));
        DataSource ds = ioc.get(DataSource.class);
        dao = new NutDao(ds);

        this.initTables();
        ioc.depose();
    }

    private void initTables() {
        for (String table : TABLES.keySet()) {
            if (!dao.exists(table)) {
                dao.create(TABLES.get(table), true);
            }
        }
    }
}
