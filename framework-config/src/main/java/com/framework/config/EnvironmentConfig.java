package com.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Typed facade around the merged YAML configuration.
 *
 * This class is DI-managed (see framework-di's PlatformModule): it is bound
 * as an eager singleton so the whole platform shares one loaded config tree,
 * and is injected into whatever needs it (AuthenticationManager, HttpClientFactory,
 * Steps classes, ...) instead of being reached via a static accessor.
 *
 * A no-arg constructor is all Guice needs to build it (JIT binding), since
 * loading is driven entirely by ConfigLoader + the classpath/env - there's
 * nothing to inject.
 */
@Slf4j
@Singleton
public class EnvironmentConfig {

    private final JsonNode config;
    private final String environmentName;

    public EnvironmentConfig() {
        this.environmentName = ConfigLoader.activeEnvironment();
        this.config = ConfigLoader.loadMergedConfig();
        log.info("EnvironmentConfig initialised for env '{}'", environmentName);
    }

    public String environmentName() {
        return environmentName;
    }

    public String baseUrl() {
        return text("api.baseUrl");
    }

    public int connectTimeoutMs() {
        return config.path("api").path("connectTimeoutMs").asInt(10000);
    }

    public int readTimeoutMs() {
        return config.path("api").path("readTimeoutMs").asInt(15000);
    }

    public int retryMaxAttempts() {
        return config.path("api").path("retry").path("maxAttempts").asInt(3);
    }

    public long retryBackoffMs() {
        return config.path("api").path("retry").path("backoffMs").asLong(500);
    }

    public boolean logOnlyOnFailure() {
        return config.path("api").path("logging").path("logOnlyOnFailure").asBoolean(true);
    }

    public boolean loggingEnabled() {
        return config.path("api").path("logging").path("requestsAndResponses").asBoolean(true);
    }

    public String tokenUrl() {
        return text("auth.tokenUrl");
    }

    public String grantType() {
        return text("auth.grantType");
    }

    public String clientId() {
        return text("auth.clientId");
    }

    public String clientSecret() {
        return text("auth.clientSecret");
    }

    public int tokenRefreshSkewSeconds() {
        return config.path("auth").path("tokenRefreshSkewSeconds").asInt(30);
    }

    public String testDataBasePath() {
        return text("testdata.basePath");
    }

    /** Generic escape hatch: dotted-path lookup for any value not exposed above. */
    public String text(String dottedPath) {
        JsonNode current = config;
        for (String part : dottedPath.split("\\.")) {
            current = current.path(part);
        }
        return current.isMissingNode() ? null : current.asText(null);
    }

    public boolean bool(String dottedPath, boolean defaultValue) {
        JsonNode current = config;
        for (String part : dottedPath.split("\\.")) {
            current = current.path(part);
        }
        return current.isMissingNode() ? defaultValue : current.asBoolean(defaultValue);
    }

    public int integer(String dottedPath, int defaultValue) {
        JsonNode current = config;
        for (String part : dottedPath.split("\\.")) {
            current = current.path(part);
        }
        return current.isMissingNode() ? defaultValue : current.asInt(defaultValue);
    }
}
