package org.albertyu.model;

import lombok.Data;

import java.util.Map;

/**
 * Configuration of application
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019-03-11 9:42 AM
 */
@Data
public class Config {
    /**
     * Website front url and API path,
     * used for applications open access from front API.
     */
    private String frontUrl;
    private String apiBasePath;

    /**
     * Website admin portal login information,
     * used for applications only allow access from backend,
     * admin cookies will be used.
     */
    private String adminUrl;
    private String adminEmail;
    private String adminPassword;
    private Map<String, String> adminCookies;

    private String articleSavePath; // Used in front API or backend

    private String recipient; // Email for receiving reports and notifications

    private int maxPastMinutes = 180; // Source article publish time
    private int maxPastHours;

    public void init() {
        this.maxPastHours = this.maxPastMinutes / 60;
    }
}
