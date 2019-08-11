package org.albertyu.utils;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:29
 */
public class HtmlUtils {

    private static void unwrapDeepLayeredHtmlTags(Element dom) {
        Elements elements = dom.children();
        int size = elements.size();
        if (size == 0) {
            return;
        }
        for (Element row : elements) {
            unwrapParent(row);
        }
    }

    private static void unwrapParent(Element element) {
        Element parent = element.parent();
        int size = parent.children().size();
        if (element.children().size() == 0 && size == 1 &&
            StringUtils.equals(parent.text().trim(), element.text().trim())) {
            parent.after(element.outerHtml());
            parent.remove();
            unwrapDeepLayeredHtmlTags(parent);
            return;
        }
        unwrapDeepLayeredHtmlTags(element);
    }

    public static String cleanHtml(Element dom) {
        removeNeedlessHtmlTags(dom);
        removeImgTagAttrs(dom);
        return removeHtmlComments(dom.html());
    }

    private static String removeHtmlComments(String html) {
        List<String> list = RegexUtils.getMatchedList(html, "<\\!--.*-->");
        for (String str : list) {
            html = StringUtils.trim(StringUtils.remove(html, str));
        }
        return html;
    }

    public static void removeNeedlessHtmlTags(Element dom) {
        if (dom == null) {
            return;
        }
        Elements elements = dom.children();
        if (elements.size() == 0) {
            return;
        }

        for (Element element : elements) {
            String tagName = element.tagName();
            if (isImageOrBreak(tagName)) {
                continue;
            }
            if (!hasContent(element)) {
                element.remove();
                continue;
            }

            // Remove tag attributes
            Attributes attributes = element.attributes();
            if (attributes.size() > 0) {
                for (Attribute attr : attributes) {
                    element.removeAttr(attr.getKey());
                }
            }

            if (!isAllowedTag(tagName)) {
                if (isBlockTag(tagName)) {
                    // Replace tag names to p
                    element.tagName("p");
                } else {
                    element.tagName("span");
                }
            }
            removeNeedlessHtmlTags(element);
        }
    }

    private static boolean isAllowedTag(String tagName) {
        return StringUtils.equalsIgnoreCase(tagName, "p") ||
            StringUtils.equalsIgnoreCase(tagName, "span") ||
            StringUtils.equalsIgnoreCase(tagName, "table") ||
            StringUtils.equalsIgnoreCase(tagName, "tr") ||
            StringUtils.equalsIgnoreCase(tagName, "th") ||
            StringUtils.equalsIgnoreCase(tagName, "tr") ||
            StringUtils.equalsIgnoreCase(tagName, "td") ||
            StringUtils.equalsIgnoreCase(tagName, "thead") ||
            StringUtils.equalsIgnoreCase(tagName, "tfoot") ||
            StringUtils.equalsIgnoreCase(tagName, "ul") ||
            StringUtils.equalsIgnoreCase(tagName, "ol") ||
            StringUtils.equalsIgnoreCase(tagName, "li") ||
            StringUtils.equalsIgnoreCase(tagName, "dl") ||
            StringUtils.equalsIgnoreCase(tagName, "dt") ||
            StringUtils.equalsIgnoreCase(tagName, "dd");
    }

    private static boolean isBlockTag(String tagName) {
        return StringUtils.equalsIgnoreCase(tagName, "div") ||
            StringUtils.equalsIgnoreCase(tagName, "h2") ||
            StringUtils.equalsIgnoreCase(tagName, "h3") ||
            StringUtils.equalsIgnoreCase(tagName, "h4");
    }

    private static boolean hasContent(Element element) {
        return hasText(element) ||
            element.select("img").size() > 0;
    }

    private static boolean hasText(Element element) {
        return StringUtils.isNotBlank(RegExUtils.removePattern(element.text(), "\\s*|\t|\r|\n"));
    }

    private static boolean isImageOrBreak(String tagName) {
        return StringUtils.equalsIgnoreCase(tagName, "img") ||
            StringUtils.equalsIgnoreCase(tagName, "br");
    }

    private static void removeImgTagAttrs(Element dom) {
        if (dom == null) {
            return;
        }
        Elements images = dom.select("img");
        if (images.size() == 0) {
            return;
        }
        images.removeAttr("width")
            .removeAttr("height")
            .removeAttr("class")
            .removeAttr("style")
            .removeAttr("srcset")
            .removeAttr("origin");
    }

    public static void justifyImage(Element image) {
        Element parent = image.parent();
        if (!hasText(parent)) {
            parent.tagName("div");
            Set<String> classNames = new HashSet<>();
            classNames.add("imageBox");
            parent.classNames(classNames);
            return;
        }
        image.after("<div class=\"imageBox\"><img src=\"" + image.attr("src") + "\" /></div>");
        image.remove();
    }
}
