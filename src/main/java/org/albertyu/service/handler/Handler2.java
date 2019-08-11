package org.albertyu.service.handler;

import com.google.inject.Inject;
import org.albertyu.model.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Access to server from API with auth tokens
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/11 21:04
 */
public class Handler2 extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(Handler2.class);
    @Inject Messengers messengers;

    @Override
    List<SavedImage> saveImages(Article article, ImageUploadResult result, Config config) {
        return null;
    }

    @Override
    protected ImageUploadResult uploadImages(Article article, WebDriver driver, Config config) {
        return null;
    }

    @Override
    void uploadArticle(Article article, WebDriver driver, Config config) {

    }

    @Override
    public void access(WebDriver driver, Config config) {

    }
}
