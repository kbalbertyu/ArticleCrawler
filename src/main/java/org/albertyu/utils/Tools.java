package org.albertyu.utils;

import com.alibaba.fastjson.JSONObject;
import org.albertyu.model.Constant;
import org.albertyu.model.ImageType;
import org.albertyu.model.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nutz.lang.Lang;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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
            return FileUtils.readFileToString(file, Constant.UTF8);
        } catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public static Config loadAppConfig() {
        String configStr = Tools.readFileToString(FileUtils.getFile(Constant.CONFIG, Constant.APP_CONFIG));
        return JSONObject.parseObject(configStr, Config.class);
    }

    public static String toMD5(String text) {
        return DigestUtils.md5Hex(text).toUpperCase();
    }

    public static boolean numericEquals(float num1, float num2) {
        return Math.abs(num1 - num2) < 0.001;
    }

    public static String percentage(float val) {
        return Constant.DOUBLE_FORMAT.format(val) + Constant.PERCENTAGE;
    }

    public static String getAbsoluteUrl(String url, String pageUrl) {
        if (StringUtils.startsWith(StringUtils.lowerCase(url), Constant.HTTP)) {
            return url;
        }

        String[] parts = StringUtils.split(pageUrl, Constant.DASH);
        if (StringUtils.startsWith(url, Constant.DOUBLE_DASH)) {
            return parts[0] + url;
        }
        String baseUrl = parts[0] + Constant.DOUBLE_DASH + parts[1];
        String path;
        if (parts.length == 2) {
            path = StringUtils.EMPTY;
        } else {
            boolean endWithDir = StringUtils.endsWith(pageUrl, Constant.DASH);
            int length = parts.length - (endWithDir ? 0 : 1);

            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < length; i++) {
                sb.append(Constant.DASH);
                sb.append(parts[i]);
            }
            path = sb.toString();
        }

        if (StringUtils.startsWith(url, Constant.PERIOD)) {
            url = baseUrl + (path.length() == 1 ? StringUtils.EMPTY : path) + StringUtils.substring(url, 1);
        } else if (StringUtils.startsWith(url, Constant.DASH)) {
            url = baseUrl + url;
        } else {
            url = baseUrl + path + Constant.DASH + url;
        }
        return url;
    }

    static String extractFileNameFromUrl(String url) {
        url = StringUtils.substringBefore(url, Constant.QUESTION_MARK);
        String[] pathParts = StringUtils.split(url, Constant.DASH);
        return pathParts[pathParts.length - 1];
    }

    static ImageType determineImageFileType(String file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            String hex = bytesToHexString(b).toUpperCase();
            return ImageType.getTypeByHex(hex);
        }
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    static void convertImageFileType(String originalUrl, String path, ImageType targetType) throws IOException {
        FileUtils.deleteQuietly(FileUtils.getFile(path));
        BufferedImage im = ImageIO.read(new URL(originalUrl));
        ImageIO.write(im, targetType.toExt(), FileUtils.getFile(path));
    }
}
