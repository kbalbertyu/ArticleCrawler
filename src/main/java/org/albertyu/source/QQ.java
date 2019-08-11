package org.albertyu.source;

import org.albertyu.model.Article;
import org.albertyu.model.CSSQuery;
import org.albertyu.model.Category;
import org.albertyu.model.Messenger;
import org.albertyu.utils.DocParser;
import org.albertyu.utils.Exceptions.PastDateException;
import org.albertyu.utils.PageUtils;
import org.albertyu.utils.PageUtils.WaitTime;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019-01-05 2:42 PM
 */
public class QQ extends Source {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Map<String, Category> URLS = new HashMap<>();

    static {
        URLS.put("https://new.qq.com/omn/author/5042880", Category.ENTERTAINMENT);
        URLS.put("https://new.qq.com/rolls/?ext=sports", Category.SPORTS);
    }

    @Override
    protected Map<String, Category> getUrls() {
        return URLS;
    }

    @Override
    protected String getDateRegex() {
        return "\\d{4}/\\d{2}/\\d{2} \\d{1,2}:\\d{1,2}";
    }

    @Override
    protected String getDateFormat() {
        return "yyyy/MM/dd HH:mm";
    }

    @Override
    protected CSSQuery getCSSQuery() {
        String idPrefix = DateFormatUtils.format(new Date(), "yyyyMMdd");
        return new CSSQuery("li[id^=" + idPrefix + "]", "h3 > a", "", "", ".content-article");
    }

    @Override
    protected List<Article> parseList(Document doc) {
        List<Article> articles = new ArrayList<>();
        Elements list = this.readList(doc);
        for (Element row : list) {
            Article article = new Article();

            this.parseTitle(row, article);
            if (StringUtils.isBlank(article.getUrl())) {
                continue;
            }

            String dateTextCssQuery = ".time";
            this.checkDateTextExistence(row, dateTextCssQuery);
            String timeText = DocParser.text(row, dateTextCssQuery);
            if (StringUtils.contains(timeText, "小时前") &&
                !this.checkHoursBefore(timeText)) {
                continue;
            }
            articles.add(article);
        }
        return articles;
    }

    @Override
    protected String cleanHtml(Element dom) {
        Elements elements = dom.select(".content-article .content-article, script, #Status, .article-status, [class*=video]");
        if (elements.size() > 0) {
            elements.remove();
        }
        return super.cleanHtml(dom);
    }

    @Override
    public void readArticle(WebDriver driver, Article article) {
        driver.get(article.getUrl());
        PageUtils.removeElementByClass(driver, "recommend");
        if (PageUtils.present(driver, By.className("LazyLoad"), WaitTime.Normal)) {
            PageUtils.loadLazyContent(driver);
        } else {
            PageUtils.scrollToBottom(driver);
        }
        WaitTime.Short.execute();
        Document doc = Jsoup.parse(driver.getPageSource());

        this.parseDate(doc, article);
        this.parseContent(doc, article);
    }

    @Override
    void checkDate(Date date) {
        if (this.calcMinutesAgo(date) > config.getMaxPastMinutes()) {
            throw new PastDateException();
        }
    }

    @Override
    void parseDate(Element doc, Article article) {
        String year = ".left-stick-wp > .year";
        String monthDay = ".left-stick-wp > .md";
        String time = ".left-stick-wp > .time";
        if (!DocParser.allExist(doc, year, monthDay, time)) {
            Messenger messenger = new Messenger(this.getClass().getName(),
                String.format("%s not found with: %s, %s and %s", "Date text", year, monthDay, time));
            this.messengers.add(messenger);
        }
        String timeText = DocParser.text(doc, year) + "/" +
            DocParser.text(doc, monthDay) + " " +
            DocParser.text(doc, time);
        article.setTimestamp(this.parseDateText(timeText).getTime());
    }

    @Override
    public void parseTitle(Element doc, Article article) {
        String titleCssQuery = this.getCSSQuery().getTitle();
        this.checkTitleExistence(doc, titleCssQuery);
        Element linkElm = doc.select(titleCssQuery).get(0);
        String title = linkElm.text();
        if (StringUtils.contains(title, "专题")) {
            logger.warn("Not an article link, just skip: {}", title);
            return;
        }
        article.setUrl(linkElm.attr("href"));
        article.setTitle(title);
    }
}
