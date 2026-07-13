package com.framework.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * Grab-bag of small, composable assertion helpers so tests read like a
 * checklist instead of re-deriving JsonPath/Hamcrest expressions each time.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static Response assertStatusCode(Response response, int expected) {
        response.then().log().ifValidationFails().statusCode(expected);
        return response;
    }

    public static Response assertStatusCodeIn(Response response, int... acceptable) {
        int actual = response.statusCode();
        boolean ok = java.util.stream.IntStream.of(acceptable).anyMatch(c -> c == actual);
        if (!ok) {
            response.then().log().all();
        }
        Assertions.assertThat(actual)
                .as("Expected status code to be one of %s but was %s. Body: %s",
                        java.util.Arrays.toString(acceptable), actual, response.asString())
                .isIn(java.util.stream.IntStream.of(acceptable).boxed().toList());
        return response;
    }

    public static Response assertMatchesSchema(Response response, String schemaClasspathPath) {
        response.then().log().ifValidationFails()
                .assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaClasspathPath));
        return response;
    }

    public static Response assertFieldEquals(Response response, String jsonPath, Object expected) {
        response.then().log().ifValidationFails().body(jsonPath, equalTo(expected));
        return response;
    }

    public static Response assertFieldExists(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        if (value == null) {
            response.then().log().all();
        }
        Assertions.assertThat(value)
                .as("Expected field '%s' to be present in response: %s", jsonPath, response.asString())
                .isNotNull();
        return response;
    }

    public static Response assertFieldIsNull(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        Assertions.assertThat(value)
                .as("Expected field '%s' to be null but was '%s'", jsonPath, value)
                .isNull();
        return response;
    }

    public static Response assertListSize(Response response, String jsonPath, int expectedSize) {
        List<?> list = response.jsonPath().getList(jsonPath);
        Assertions.assertThat(list)
                .as("Expected list at '%s' to have size %d", jsonPath, expectedSize)
                .hasSize(expectedSize);
        return response;
    }

    public static Response assertListContains(Response response, String jsonPath, Object expectedItem) {
        List<?> list = response.jsonPath().getList(jsonPath);
        Assertions.assertThat(list)
                .as("Expected list at '%s' to contain '%s'", jsonPath, expectedItem)
                .contains(expectedItem);
        return response;
    }

    public static Response assertHeaderEquals(Response response, String headerName, String expected) {
        String actual = response.header(headerName);
        Assertions.assertThat(actual)
                .as("Expected header '%s' to equal '%s'", headerName, expected)
                .isEqualTo(expected);
        return response;
    }

    public static Response assertHeaderExists(Response response, String headerName) {
        Assertions.assertThat(response.header(headerName))
                .as("Expected header '%s' to be present", headerName)
                .isNotNull();
        return response;
    }

    public static Response assertResponseTimeUnder(Response response, long maxMillis) {
        long actual = response.time();
        Assertions.assertThat(actual)
                .as("Expected response time under %dms but was %dms", maxMillis, actual)
                .isLessThanOrEqualTo(maxMillis);
        return response;
    }

    public static void requireNonBlank(String value, String fieldName) {
        Assertions.assertThat(value)
                .as("Required field '%s' must not be blank before making the request", fieldName)
                .isNotBlank();
    }

    public static void requireNonNull(Object value, String fieldName) {
        Assertions.assertThat(value)
                .as("Required field '%s' must not be null before making the request", fieldName)
                .isNotNull();
    }

    public static void requireFields(Map<String, Object> payload, String... requiredKeys) {
        for (String key : requiredKeys) {
            Assertions.assertThat(payload)
                    .as("Payload is missing required key '%s'", key)
                    .containsKey(key);
            Assertions.assertThat(payload.get(key))
                    .as("Payload key '%s' must not be null", key)
                    .isNotNull();
        }
    }
}
