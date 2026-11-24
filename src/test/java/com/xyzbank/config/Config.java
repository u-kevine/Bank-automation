package com.xyzbank.config;

public final class Config {
    public static final String BASE_URL = env("BASE_URL",
            "https://www.globalsqa.com/angularJs-protractor/BankingProject/#/login");
    public static final String BROWSER = env("BROWSER", "chrome").toLowerCase();
    public static final boolean HEADLESS = Boolean.parseBoolean(env("HEADLESS", "true"));
    public static final String SELENIUM_REMOTE_URL = env("SELENIUM_REMOTE_URL", "");
    public static final int TIMEOUT = Integer.parseInt(env("TIMEOUT", "15"));

    private Config() {
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}