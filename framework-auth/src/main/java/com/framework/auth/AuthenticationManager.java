package com.framework.auth;

import com.framework.config.EnvironmentConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Central service responsible for getting a valid bearer token before an API
 * call happens, so individual tests never have to think about auth.
 *
 * DI-managed as a platform-wide singleton (see framework-di's PlatformModule)
 * so every Steps class shares the same token cache instead of each re-fetching
 * its own. Constructor-injected with EnvironmentConfig rather than reaching
 * for a static accessor - this is the main thing that changed from the
 * original single-module version of this framework.
 *
 * Supports multiple "roles"/personas (e.g. "admin", "doctor", "patient") each
 * with their own client credentials. Tokens are cached per role and refreshed
 * automatically once they get close to expiry; a per-role lock keeps refreshes
 * for different roles from blocking each other under parallel execution.
 *
 * Usage (typically via constructor injection into a Steps class):
 *   public class UserSteps {
 *       @Inject AuthenticationManager auth;
 *       ...
 *       String token = auth.getToken("admin");
 *   }
 */
@Slf4j
@Singleton
public class AuthenticationManager {

    private final EnvironmentConfig config;
    private final Map<String, Credentials> roleCredentials = new ConcurrentHashMap<>();
    private final Map<String, TokenResponse> tokenCache = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Inject
    public AuthenticationManager(EnvironmentConfig config) {
        this.config = config;
        registerRole("default", config.clientId(), config.clientSecret());
    }

    /** Register (or override) the client credentials backing a named role/persona. */
    public void registerRole(String role, String clientId, String clientSecret) {
        roleCredentials.put(role, new Credentials(clientId, clientSecret));
        locks.putIfAbsent(role, new ReentrantLock());
    }

    /**
     * Returns a ready-to-use "Bearer xxx" header value for the given role,
     * fetching a fresh token if none is cached yet or the cached one is
     * expired/expiring soon.
     */
    public String getToken(String role) {
        TokenResponse cached = tokenCache.get(role);
        int skew = config.tokenRefreshSkewSeconds();

        if (cached != null && !cached.isExpiredOrExpiringWithin(skew)) {
            return cached.authorizationHeaderValue();
        }

        ReentrantLock lock = locks.computeIfAbsent(role, r -> new ReentrantLock());
        lock.lock();
        try {
            TokenResponse recheck = tokenCache.get(role);
            if (recheck != null && !recheck.isExpiredOrExpiringWithin(skew)) {
                return recheck.authorizationHeaderValue();
            }
            TokenResponse fresh = fetchNewToken(role);
            tokenCache.put(role, fresh);
            return fresh.authorizationHeaderValue();
        } finally {
            lock.unlock();
        }
    }

    /** Convenience overload for the single-tenant / default-role case. */
    public String getToken() {
        return getToken("default");
    }

    /** Force-evict a cached token, e.g. after a test that deliberately revokes/logs out. */
    public void invalidate(String role) {
        tokenCache.remove(role);
    }

    private TokenResponse fetchNewToken(String role) {
        Credentials creds = roleCredentials.get(role);
        if (creds == null) {
            throw new IllegalStateException(
                    "No credentials registered for role '" + role + "'. Call registerRole(...) first.");
        }
        log.info("Fetching new auth token for role '{}' from {}", role, config.tokenUrl());

        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", config.grantType())
                .formParam("client_id", creds.clientId())
                .formParam("client_secret", creds.clientSecret())
                .when()
                .post(config.tokenUrl())
                .then()
                .extract().response();

        if (response.statusCode() >= 400) {
            throw new IllegalStateException(
                    "Failed to obtain auth token for role '" + role + "': HTTP " + response.statusCode()
                            + " - " + response.asString());
        }

        TokenResponse token = response.as(TokenResponse.class);
        log.debug("Obtained token for role '{}', expires in {}s", role, token.getExpiresInSeconds());
        return token;
    }

    private record Credentials(String clientId, String clientSecret) {
    }
}
