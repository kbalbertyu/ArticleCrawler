package org.albertyu.source;

import com.google.inject.Inject;
import org.albertyu.database.DBManager;
import org.albertyu.model.Article;
import org.albertyu.model.CSSQuery;
import org.albertyu.model.Category;
import org.albertyu.model.Messengers;
import org.albertyu.model.config.Config;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 7:01
 */
public abstract class Source {
    private final Logger logger = LoggerFactory.getLogger(Source.class);
    private static final String WITHOUT_YEAR = "1970";
    private static final String WITHOUT_MONTH_DAY = "01/01";
    private static final String DOWNLOAD_PATH = "downloads";
    @Inject private DBManager dbManager;
    @Inject Messengers messengers;
    protected Config config;

    protected abstract Map<String, Category> getUrls();

    protected abstract String getDateRegex();

    protected abstract String getDateFormat();

    protected abstract CSSQuery getCSSQuery();

    protected abstract List<Article> parseList(Document doc);

    protected abstract void readArticle(WebDriver driver, Article article);

    boolean withoutDriver() {
        return false;
    }
}
