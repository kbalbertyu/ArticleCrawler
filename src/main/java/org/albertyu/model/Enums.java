package org.albertyu.model;

import org.albertyu.service.InstanceCaller;
import org.albertyu.service.handler.AbstractHandler;
import org.albertyu.service.handler.Handler1;
import org.albertyu.service.handler.Handler2;

import java.io.File;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/22 4:31
 */
public class Enums {

    public enum ChromeVersion {
        V740("74.0", 74, 74),
        V730("73.0", 73, 73),
        V720("72.0", 72, 72);

        private static final String WEB_DRIVER_HOME = "drivers";
        private final String version;
        private final int minChromeVersion;
        private final int maxChromeVersion;

        public String filePath() {
            return WEB_DRIVER_HOME + File.separator + "chromedriver" + version + ".exe";
        }

        ChromeVersion(String version, int minChromeVersion, int maxChromeVersion) {
            this.version = version;
            this.minChromeVersion = minChromeVersion;
            this.maxChromeVersion = maxChromeVersion;
        }
        public int[] chromeVersionRange() {
            return new int[] {this.minChromeVersion, this.maxChromeVersion};
        }

        @Override
        public String toString() {
            return String.format("Chrome driver: %s, for version %s ~ %s", this.version, this.minChromeVersion, this.maxChromeVersion);
        }
    }

    public enum ArticleHandler {
        Handler1(InstanceCaller.getBean(Handler1.class)),
        Handler2(InstanceCaller.getBean(Handler2.class));

        public final AbstractHandler handler;

        ArticleHandler(AbstractHandler handler) {
            this.handler = handler;
        }
    }
}
