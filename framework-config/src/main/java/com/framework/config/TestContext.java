package com.framework.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-thread scratch space, conceptually the same idea as a Postman
 * "environment": a bag of key/value variables that steps within the SAME
 * test (running on the same thread) can write to and read from.
 *
 * Deliberately NOT DI-managed: this is thread-local *state*, not a service
 * with dependencies, so a plain static utility is the right shape for it -
 * injecting it would only add ceremony for no benefit since there is never
 * more than one logical instance per thread regardless of who asks for it.
 */
public final class TestContext {

    private static final ThreadLocal<Map<String, Object>> VARIABLES =
            ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<String> CORRELATION_ID =
            ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private static final Map<String, Object> SHARED = new ConcurrentHashMap<>();

    private TestContext() {
    }

    public static void set(String key, Object value) {
        VARIABLES.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) VARIABLES.get().get(key);
    }

    public static <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    public static boolean contains(String key) {
        return VARIABLES.get().containsKey(key);
    }

    public static void remove(String key) {
        VARIABLES.get().remove(key);
    }

    public static void clear() {
        VARIABLES.get().clear();
        CORRELATION_ID.remove();
    }

    public static String correlationId() {
        return CORRELATION_ID.get();
    }

    public static void newCorrelationId() {
        CORRELATION_ID.set(UUID.randomUUID().toString());
    }

    public static Object sharedGet(String key) {
        return SHARED.get(key);
    }

    public static void sharedPut(String key, Object value) {
        SHARED.put(key, value);
    }

    public static Object sharedComputeIfAbsent(String key, java.util.function.Function<String, Object> supplier) {
        return SHARED.computeIfAbsent(key, supplier);
    }
}
