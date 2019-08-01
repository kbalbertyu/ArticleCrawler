package org.albertyu.utils;

import com.alibaba.fastjson.JSONObject;
import org.albertyu.model.Constant;
import org.albertyu.model.config.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nutz.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/5/22 4:41
 */
public class Tools {

    private final static String CHROME_VERSION_REGEX = "[0-9]{1,3}.[0-9]{1,2}.[0-9]{3,5}.[0-9]{2,4}";

    /**
     * Fetch current Chrome browser major version number in Windows OS
     */
    public static int getChromeMajorVersion() {
        String[] roots = {SystemUtils.getUserHome().getAbsolutePath() + "/AppData/Local/Google/Chrome/Application",
            "C:/Program Files (x86)/Google/Chrome/Application"};
        File[] dirs = null;
        String installPath = null;
        for (String root : roots) {
            dirs = new File(root).listFiles(folder -> folder.isDirectory() && match(folder.getName(), CHROME_VERSION_REGEX));
            if (ArrayUtils.isNotEmpty(dirs)) {
                installPath = root;
                break;
            }
        }

        if (dirs == null || ArrayUtils.isEmpty(dirs)) {
            return -1;
        }
        List<Integer> versions = new ArrayList<>(dirs.length);
        for (File dir : dirs) {
            // Full version format like: 74.0.3729.169
            versions.add(NumberUtils.toInt(dir.getName().substring(0, dir.getName().indexOf("."))));
        }
        Collections.sort(versions);

        int index = versions.size() - 1;
        // When Chrome prepares for upgrading, a new_chrome.exe file will be generated, should use former Chrome version instead.
        if (new File(installPath, "new_chrome.exe").exists() && versions.size() >= 2) {
            index = versions.size() - 2;
        }
        return versions.get(index);
    }

    private static boolean match(String source, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);
        return matcher.matches();
    }

    public static void sleep(long timeInSecond) {
        sleep(timeInSecond, TimeUnit.SECONDS);
    }

    private static void sleep(long time, TimeUnit timeunit) {
        try {
            timeunit.sleep(time);
        } catch (InterruptedException e) {
            // -> Ignore
        }
    }

    private static String readFileToString(File file) {
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public static Config loadAppConfig() {
        String configStr = Tools.readFileToString(FileUtils.getFile(Constant.CONFIG, "app.json"));
        return JSONObject.parseObject(configStr, Config.class);
    }
}
