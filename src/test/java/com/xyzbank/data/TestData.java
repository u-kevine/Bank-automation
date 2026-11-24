package com.xyzbank.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public final class TestData {
    private static final JsonNode ROOT = load();

    private TestData() {
    }

    private static JsonNode load() {
        try (InputStream in = TestData.class.getClassLoader().getResourceAsStream("testdata.json")) {
            if (in == null) {
                throw new IllegalStateException("testdata.json not found on the classpath");
            }
            return new ObjectMapper().readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException("testdata.json could not be read", e);
        }
    }

    public static JsonNode get(String section) {
        JsonNode node = ROOT.get(section);
        if (node == null) {
            throw new IllegalArgumentException("No section '" + section + "' in testdata.json");
        }
        return node;
    }

    public static String message(String key) {
        return get("messages").get(key).asText();
    }

    public static List<String> strings(String section) {
        List<String> values = new ArrayList<>();
        get(section).forEach(n -> values.add(n.asText()));
        return values;
    }

    public static List<Integer> ints(String section, String key) {
        List<Integer> values = new ArrayList<>();
        get(section).get(key).forEach(n -> values.add(n.asInt()));
        return values;
    }

    public static int intValue(String section, String key) {
        return get(section).get(key).asInt();
    }

    public static Object[][] rows(List<?> values) {
        Object[][] rows = new Object[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            rows[i] = new Object[] {values.get(i)};
        }
        return rows;
    }
}
