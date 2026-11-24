package com.xyzbank.driver;

import com.xyzbank.config.Config;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class DriverFactory {
    private static final Logger SELENIUM_LOGGER = Logger.getLogger("org.openqa.selenium");

    static {
        SELENIUM_LOGGER.setLevel(Level.SEVERE);
    }

    private DriverFactory() {
    }

    public static WebDriver create() {
        boolean firefox = "firefox".equals(Config.BROWSER);
        MutableCapabilities options = firefox ? firefoxOptions() : chromeOptions();

        WebDriver driver;
        if (!Config.SELENIUM_REMOTE_URL.isEmpty()) {
            driver = new RemoteWebDriver(toUrl(Config.SELENIUM_REMOTE_URL), options);
        } else if (firefox) {
            driver = new FirefoxDriver((FirefoxOptions) options);
        } else {
            driver = new ChromeDriver((ChromeOptions) options);
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        return driver;
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (Config.HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        if (Config.HEADLESS) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }

    private static java.net.URL toUrl(String url) {
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid SELENIUM_REMOTE_URL: " + url, e);
        }
    }
}
