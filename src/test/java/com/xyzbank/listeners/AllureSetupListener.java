package com.xyzbank.listeners;

import com.xyzbank.config.Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class AllureSetupListener implements ISuiteListener {
    private static final Path RESULTS = Path.of("allure-results");

    @Override
    public void onStart(ISuite suite) {
        try {
            Files.createDirectories(RESULTS);
            emptyFolder(RESULTS);
            String env = String.join("\n",
                    "Suite=XYZ Bank - Full",
                    "Java=" + System.getProperty("java.version"),
                    "Browser=" + Config.BROWSER + (Config.HEADLESS ? " (headless)" : ""),
                    "Selenium Grid=" + (Config.SELENIUM_REMOTE_URL.isEmpty() ? "local browser" : Config.SELENIUM_REMOTE_URL),
                    "Base URL=" + Config.BASE_URL);
            Files.writeString(RESULTS.resolve("environment.properties"), env, StandardCharsets.UTF_8);
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("categories.json")) {
                if (in != null) {
                    Files.copy(in, RESULTS.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not prepare " + RESULTS, e);
        }
    }

    private static void emptyFolder(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                if (!p.equals(folder)) {
                    Files.delete(p);
                }
            }
        }
    }
}