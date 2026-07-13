package com.framework.http;

import com.framework.auth.AuthenticationManager;
import com.framework.config.EnvironmentConfig;
import com.framework.config.TestContext;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * DI-managed factory for REST Assured RequestSpecifications, so every Steps
 * class gets consistent base URL, timeouts, logging and reporting behaviour
 * without repeating boilerplate. Constructor-injected with EnvironmentConfig
 * and AuthenticationManager (see framework-di's PlatformModule) instead of
 * reaching for static accessors.
 *
 * Usage (typically via @Inject into a Steps class):
 *   public class UserSteps {
 *       @Inject HttpClientFactory http;
 *       ...
 *       http.spec().withAuth("admin").request()
 *           .body(payload)
 *           .post("/users");
 *   }
 */
@Singleton
public class HttpClientFactory {

    private final EnvironmentConfig config;
    private final AuthenticationManager authManager;

    @Inject
    public HttpClientFactory(EnvironmentConfig config, AuthenticationManager authManager) {
        this.config = config;
        this.authManager = authManager;
    }

    /** Start building a request spec for the current environment (baseUrl comes from application-{env}.yml). */
    public Builder spec() {
        return new Builder(config.baseUrl());
    }

    /**
     * Start building a request spec against an explicit base URI instead of the
     * environment's configured baseUrl. Useful for hitting a fixed public/demo
     * API that isn't the system under test - see the examples.* packages in
     * test-execution.
     */
    public Builder spec(String explicitBaseUri) {
        return new Builder(explicitBaseUri);
    }

    public final class Builder {
        private final RequestSpecBuilder specBuilder = new RequestSpecBuilder();

        private Builder(String baseUri) {
            specBuilder
                    .setBaseUri(baseUri)
                    .setContentType(ContentType.JSON)
                    .addHeader("X-Correlation-Id", TestContext.correlationId())
                    .setConfig(RestAssuredConfig.config().httpClient(
                            HttpClientConfig.httpClientConfig()
                                    .setParam("http.connection.timeout", config.connectTimeoutMs())
                                    .setParam("http.socket.timeout", config.readTimeoutMs())
                    ));

            // Always attach the Allure filter so every call shows up nicely in the HTML report
            specBuilder.addFilter(new AllureRestAssured());

            if (config.loggingEnabled() && !config.logOnlyOnFailure()) {
                specBuilder.addFilter(new RequestLoggingFilter());
                specBuilder.addFilter(new ResponseLoggingFilter());
            }
        }

        /** Injects "Authorization: Bearer xxx" for the given role via AuthenticationManager. */
        public Builder withAuth(String role) {
            specBuilder.addHeader("Authorization", authManager.getToken(role));
            return this;
        }

        /** Injects auth using the default role - shorthand for withAuth("default"). */
        public Builder withAuth() {
            return withAuth("default");
        }

        public Builder header(String name, String value) {
            specBuilder.addHeader(name, value);
            return this;
        }

        public Builder queryParam(String name, Object value) {
            specBuilder.addQueryParam(name, value);
            return this;
        }

        public Builder contentType(ContentType type) {
            specBuilder.setContentType(type);
            return this;
        }

        public RequestSpecification request() {
            return specBuilder.build();
        }
    }
}
