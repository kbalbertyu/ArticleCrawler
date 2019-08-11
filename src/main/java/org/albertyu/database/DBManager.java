package org.albertyu.database;

import com.google.inject.Inject;
import org.albertyu.model.ActionLog;
import org.albertyu.model.Article;
import org.albertyu.model.TableInterface;
import org.nutz.dao.impl.NutDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/7/20 15:25
 */
public class DBManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    private final static Map<String, Class<? extends TableInterface>> TABLES = new HashMap<>();
    static {
        TABLES.put("article", Article.class);
        TABLES.put("action_log", ActionLog.class);
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

    public <T> T readById(String pkId, Class<T> clazz) {
        return dao.fetch(clazz, pkId);
    }

    public <T extends TableInterface> void save(List<T> list, Class<T> clazz) {
        for (T t : list) {
            this.save(t, clazz);
        }
    }

    public <T extends TableInterface> T save(T t, Class<T> clazz) {
        try {
            T exist = this.readById(t.getPK(), clazz);
            if (exist != null) {
                logger.debug("Record {} exists already, no need to save.", t.toString());
                return exist;
            }

            return dao.fastInsert(t);
        } catch (Exception e) {
            logger.error("Fail to insert record {} -> {} to {}:", t.getPK(), t.toString(), clazz, e);
            return t;
        }
    }
}
