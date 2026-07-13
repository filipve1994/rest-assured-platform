package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * One shared, correctly-configured Jackson ObjectMapper for the whole
 * platform, plus convenience methods for the things tests do constantly:
 * turn a POJO into JSON, turn JSON/a fixture file into a POJO, and compare
 * two JSON documents ignoring formatting/field order.
 *
 * Deliberately framework-config-agnostic: pass the full classpath path
 * (e.g. "testdata/create_user.json") rather than relying on an injected
 * base-path setting, so this module has zero dependency on framework-config.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to serialize object to JSON: " + object, e);
        }
    }

    public static String toPrettyJson(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to pretty-print object to JSON: " + object, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to deserialize JSON into " + type.getSimpleName(), e);
        }
    }

    public static JsonNode toNode(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse JSON string into a tree", e);
        }
    }

    /**
     * Loads a JSON fixture from the classpath (e.g. "testdata/create_user.json",
     * resolved against src/test/resources of whichever module is running) and
     * deserializes it into the given type.
     */
    public static <T> T loadTestData(String classpathPath, Class<T> type) {
        return fromJson(readClasspathResource(classpathPath), type);
    }

    /** Same as {@link #loadTestData(String, Class)} but returns the raw parsed JsonNode - handy for partial payloads. */
    public static JsonNode loadTestDataAsNode(String classpathPath) {
        return toNode(readClasspathResource(classpathPath));
    }

    public static String readClasspathResource(String classpathPath) {
        try (InputStream in = JsonUtils.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (in == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + classpathPath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read classpath resource: " + classpathPath, e);
        }
    }

    /**
     * Deep-compares two JSON strings for logical equality (ignores key order,
     * whitespace/formatting).
     */
    public static boolean jsonEquals(String actualJson, String expectedJson) {
        return toNode(actualJson).equals(toNode(expectedJson));
    }
}
