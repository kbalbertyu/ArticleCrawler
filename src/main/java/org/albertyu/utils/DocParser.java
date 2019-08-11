package org.albertyu.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 20:44
 */
public class DocParser {
    public static boolean anyExist(Element doc, String... selectors) {
        Elements elements = elements(doc, selectors);
        return CollectionUtils.isNotEmpty(elements);
    }

    public static boolean allExist(Element doc, String... selectors) {
        for (String selector : selectors) {
            if (doc.select(selector).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static Elements elements(Element doc, String... selectors) {
        Elements elements = null;
        for (String selector : selectors) {
            elements = doc.select(selector);
            if (CollectionUtils.isNotEmpty(elements)) {
                return elements;
            }
        }
        return elements;
    }

    public static String text(Element doc, String... selectors) {
        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (elements.size() > 0) {
                return elements.get(0).text().trim();
            }
        }
        return StringUtils.EMPTY;
    }
}
