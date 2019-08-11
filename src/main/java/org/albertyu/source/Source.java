package org.albertyu.source;

import com.google.inject.Inject;
import org.albertyu.model.*;
import org.albertyu.utils.*;
import org.albertyu.utils.Exceptions.BusinessException;
import org.albertyu.utils.Exceptions.PastDateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 7:01
 */
public abstract class Source {
    private final Logger logger = LoggerFactory.getLogger(Source.class);
    private static final String WITHOUT_YEAR = "1970";
    private static final String WITHOUT_MONTH_DAY = "01/01";
    @Inject Messengers messengers;
    protected Config config;

    protected abstract Map<String, Category> getUrls();

    protected abstract String getDateRegex();

    protected abstract String getDateFormat();

    protected abstract CSSQuery getCSSQuery();

    protected abstract List<Article> parseList(Document doc);

    public abstract void readArticle(WebDriver driver, Article article);

    boolean withoutDriver() {
        return false;
    }

    public List<Article> fetchList(WebDriver driver, Config config) {
        List<Article> articles = new ArrayList<>();
        Map<String, Category> urls = this.getUrls();
        for (String url : urls.keySet()) {
            Document doc = PageUtils.openPage(driver, url, this.withoutDriver());
            try {
                List<Article> articlesNew = this.parseList(doc);
                if (articlesNew.size() == 0) {
                    String fileName = this.getClass().getSimpleName() + System.currentTimeMillis();
                    logger.warn(fileName + ": " + url);
                }
                articlesNew.forEach(article -> {
                    article.setCategory(urls.get(url));
                    Map<Category, Integer> categories = config.getCategories();
                    Category category = article.getCategory();
                    if (categories != null && categories.containsKey(category)) {
                        article.setCategoryId(categories.get(category));
                    }
                    article.setUrl(Tools.getAbsoluteUrl(article.getUrl(), url));
                });
                articles.addAll(articlesNew);
            } catch (BusinessException e) {
                logger.error("Error found in parsing list, skip current list: ", e);
            }
        }
        logger.info("Found {} articles from list.", articles.size());
        return articles;
    }

    Elements readList(Document doc) {
        String cssQuery = this.getCSSQuery().getList();
        this.checkArticleListExistence(doc, cssQuery);
        return doc.select(cssQuery);
    }

    private Document openArticlePage(WebDriver driver, Article article) {
        return PageUtils.openArticlePage(driver, article, this.withoutDriver());
    }

    void readContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);
        this.parseContent(doc, article);
    }

    void readDateSummaryContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);

        this.parseDate(doc, article);
        this.parseSummary(doc, article);
        this.parseContent(doc, article);
    }

    void readSummaryContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);

        this.parseSummary(doc, article);
        this.parseContent(doc, article);
    }

    void readTitleContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);

        this.parseTitle(doc, article);
        this.parseContent(doc, article);
    }

    void readTitleDateContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);

        this.parseTitle(doc, article);
        this.parseDate(doc, article);
        this.parseContent(doc, article);
    }

    void readDateContent(WebDriver driver, Article article) {
        Document doc = this.openArticlePage(driver, article);

        this.parseDate(doc, article);
        this.parseContent(doc, article);
    }

    protected void parseDateTitleSummaryList(List<Article> articles, Elements list) {
        int i = 0;
        for (Element row : list) {
            try {
                Article article = new Article();
                this.parseDate(row, article);
                this.parseTitle(row, article);
                this.parseSummary(row, article);

                articles.add(article);
            } catch (PastDateException e) {
                if (i++ < Constant.MAX_REPEAT_TIMES) {
                    continue;
                }
                logger.warn("Article that past {} minutes detected, complete the list fetching: ",
                    config.getMaxPastMinutes(), e);
                break;
            }
        }
    }

    protected void parseDateTitleList(List<Article> articles, Elements list) {
        int i = 0;
        for (Element row : list) {
            try {
                Article article = new Article();
                this.parseDate(row, article);
                this.parseTitle(row, article);

                articles.add(article);
            } catch (PastDateException e) {
                if (i++ < Constant.MAX_REPEAT_TIMES) {
                    continue;
                }
                logger.warn("Article that past {} minutes detected, complete the list fetching: ",
                    config.getMaxPastMinutes(), e);
                break;
            }
        }
    }

    public void parseTitle(Element doc, Article article) {
        CSSQuery cssQuery = this.getCSSQuery();
        this.checkTitleExistence(doc, cssQuery.getTitle());
        Element linkElm = doc.select(cssQuery.getTitle()).get(0);
        if (StringUtils.isBlank(article.getUrl())) {
            article.setUrl(linkElm.attr("href"));
        }
        article.setTitle(linkElm.text());
    }

    protected void parseSummary(Element doc, Article article) {
        CSSQuery cssQuery = this.getCSSQuery();
        if (StringUtils.isBlank(cssQuery.getSummary())) {
            return;
        }
        this.checkSummaryExistence(doc, cssQuery.getSummary());
        String source = DocParser.text(doc, cssQuery.getSummary());
        article.setSummary(source);
    }

    protected void parseContent(Document doc, Article article) {
        CSSQuery cssQuery = this.getCSSQuery();
        this.checkArticleContentExistence(doc, cssQuery.getContent());
        Elements contentElms = doc.select(cssQuery.getContent());
        if (contentElms.size() == 0) {
            throw new BusinessException(String.format("Content not found with selector: %s", cssQuery.getContent()));
        }
        Element contentElm = contentElms.first();
        article.setContent(this.cleanHtml(contentElm));
        this.fetchContentImages(article, contentElm);
    }

    private void checkArticleListExistence(Element doc, String cssQuery) {
        this.checkElementExistence(doc, cssQuery, "Article list");
    }

    private void checkArticleContentExistence(Element doc, String cssQuery) {
        this.checkElementExistence(doc, cssQuery, "Article content");
    }

    void checkDateTextExistence(Element doc, String cssQuery) {
        this.checkElementExistence(doc, cssQuery, "Date text");
    }

    void checkTitleExistence(Element doc, String cssQuery) {
        this.checkElementExistence(doc, cssQuery, "Title");
    }

    private void checkSummaryExistence(Element doc, String cssQuery) {
        this.checkElementExistence(doc, cssQuery, "Summary");
    }

    private void checkElementExistence(Element doc, String cssQuery, String name) {
        if (DocParser.anyExist(doc, cssQuery)) {
            return;
        }
        Messenger messenger = new Messenger(this.getClass().getName(),
            String.format("%s not found with: %s", name, cssQuery));
        this.messengers.add(messenger);
    }

    private void fetchContentImages(Article article, Element contentElm) {
        Elements images = contentElm.select("img");
        List<String> contentImages = new ArrayList<>();
        for (Element image : images) {
            String src = image.attr("src");
            if (StringUtils.containsIgnoreCase(src, "data:image") || StringUtils.containsIgnoreCase(src, "base64")) {
                continue;
            }
            contentImages.add(src);
        }
        article.setContentImages(contentImages);
    }

    void parseDate(Element doc, Article article) {
        CSSQuery cssQuery = this.getCSSQuery();
        this.checkDateTextExistence(doc, cssQuery.getTime());
        String timeText = DocParser.text(doc, cssQuery.getTime());
        article.setTimestamp(this.parseDateText(timeText).getTime());
    }

    Date parseDescribableDateText(String timeText) {
        if (StringUtils.contains(timeText, "刚刚")) {
            return new Date();
        }
        int minutes = NumberUtils.toInt(RegexUtils.getMatched(timeText, "\\d+"));
        if (minutes == 0) {
            throw new BusinessException("Unable to parse time text: " + timeText);
        }
        if (StringUtils.contains(timeText, "小时")) {
            minutes *= 60;
        } else if (!StringUtils.contains(timeText, "分")) {
            throw new PastDateException("Time text doesn't contain minutes or hours: " + timeText);
        }

        if (minutes > config.getMaxPastMinutes()) {
            throw new PastDateException("Time has past limit: " + timeText);
        }
        return DateUtils.addMinutes(new Date(), -1 * minutes);
    }

    protected Date parseDateText(String timeText) {
        return this.parseDateText(timeText, this.getDateRegex(), this.getDateFormat());
    }

    private Date parseDateText(String timeText, String regex, String dateFormat) {
        String timeTextClean = RegexUtils.getMatched(timeText, regex);
        try {
            Date date = DateUtils.parseDate(timeTextClean, Locale.PRC, dateFormat);

            // If dateFormat without year, set as current year
            if (WITHOUT_YEAR.equals(FastDateFormat.getInstance("yyyy").format(date))) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                date = DateUtils.setYears(date, year);
            }
            // If dateFormat without month and day, set today
            if (WITHOUT_MONTH_DAY.equals(FastDateFormat.getInstance("MM/dd").format(date))) {
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                date = DateUtils.setMonths(date, month);
                date = DateUtils.setDays(date, day);
            }

            this.checkDate(date);
            return date;
        } catch (ParseException e) {
            throw new BusinessException(String.format("Unable to parse date: %s -> %s", timeText, timeTextClean));
        } catch (PastDateException e) {
            throw new PastDateException(String.format("Time has past limit: %s -> %s.", timeText, timeTextClean));
        }
    }

    void checkDate(Date date) {
        if (this.calcMinutesAgo(date) > config.getMaxPastMinutes()) {
            throw new PastDateException();
        }
    }

    Date parseDateTextWithDay(String timeText, String regex, String dateFormat, int maxPastDays) {
        try {
            timeText = RegexUtils.getMatched(timeText, regex);
            Date date = DateUtils.parseDate(timeText, Locale.PRC, dateFormat);

            if (Days.daysBetween(new DateTime(date), DateTime.now()).getDays() > maxPastDays) {
                throw new PastDateException();
            }
            return date;
        } catch (ParseException e) {
            throw new BusinessException(String.format("Unable to parse date: %s", timeText));
        }
    }

    protected String cleanHtml(Element dom) {
        return HtmlUtils.cleanHtml(dom);
    }

    private static int extractFromHoursBefore(String timeText) {
        String hoursAgo = "小时前";
        String hours = RegexUtils.getMatched(timeText, "(\\d+)" + hoursAgo);
        hours = StringUtils.remove(hours, hoursAgo);
        return StringUtils.isBlank(hours) ? 9999 : NumberUtils.toInt(hours);
    }

    boolean checkHoursBefore(String timeText) {
        int hours = extractFromHoursBefore(timeText);
        return hours <= config.getMaxPastMinutes() / 60;
    }

    int calcMinutesAgo(Date date) {
        return Minutes.minutesBetween(new DateTime(date), DateTime.now()).getMinutes();
    }
}
