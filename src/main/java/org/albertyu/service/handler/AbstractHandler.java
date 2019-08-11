package org.albertyu.service.handler;

import com.google.inject.Inject;
import org.albertyu.model.*;
import org.albertyu.utils.HtmlUtils;
import org.albertyu.utils.Tools;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:05
 */
public abstract class AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
    @Inject Messengers messengers;

    public void saveArticle(Article article, WebDriver driver, Config config) {
        article.validate();
        if (article.hasImages()) {
            ImageUploadResult result = this.uploadImages(article, driver, config);
            if (result != null) {
                List<SavedImage> savedImages = saveImages(article, result, config);
                this.deleteDownloadedImages(savedImages);
                this.replaceImages(article, savedImages, config);
            }
        }
        this.cleanThirdPartyImages(article, config);
        article.validate();
        this.uploadArticle(article, driver, config);
    }

    private void deleteDownloadedImages(List<SavedImage> savedImages) {
        if (CollectionUtils.isEmpty(savedImages)) {
            return;
        }
        for (SavedImage image : savedImages) {
            FileUtils.deleteQuietly(FileUtils.getFile(image.getPath()));
        }
    }

    private void replaceImages(Article article, List<SavedImage> savedImages, Config config) {
        if (CollectionUtils.isEmpty(savedImages)) {
            return;
        }
        String content = article.getContent();
        int[] imageIds = new int[savedImages.size()];
        for (String contentImage : article.getContentImages()) {
            String hex = Tools.toMD5(contentImage);
            for (SavedImage savedImage : savedImages) {
                if (StringUtils.startsWith(savedImage.getOriginalFile(), hex)) {
                    String newImage = config.getCdnUrl() + savedImage.getPath();
                    content = StringUtils.replace(content, contentImage, newImage);
                    break;
                }
            }
        }
        article.setContent(content);

        int i = 0;
        for (SavedImage savedImage : savedImages) {
            imageIds[i++] = savedImage.getImageId();
        }
        article.setImageIds(imageIds);
    }

    private void cleanThirdPartyImages(Article article, Config config) {
        if (!article.hasImages()) {
            return;
        }
        Element dom = Jsoup.parse(article.getContent()).body();
        boolean hasRemoval = false;
        for (Element image : dom.select("img")) {
            if (StringUtils.containsIgnoreCase(image.attr("src"), config.getCdnUrl())) {
                HtmlUtils.justifyImage(image);
                continue;
            }
            image.remove();
            hasRemoval = true;
        }
        if (hasRemoval) {
            HtmlUtils.removeNeedlessHtmlTags(dom);
        }
        article.setContent(dom.select("body").html());
    }


    abstract List<SavedImage> saveImages(Article article, ImageUploadResult result, Config config);

    abstract ImageUploadResult uploadImages(Article article, WebDriver driver, Config config);

    abstract void uploadArticle(Article article, WebDriver driver, Config config);

    public abstract void access(WebDriver driver, Config config);
}
