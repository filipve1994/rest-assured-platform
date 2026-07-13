package com.framework.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Maps a standard OAuth2 / OIDC token endpoint response.
 * Extend with extra fields (id_token, scope, ...) as your API requires.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresInSeconds;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private transient Instant issuedAt = Instant.now();

    public boolean isExpiredOrExpiringWithin(int skewSeconds) {
        Instant expiryWithSkew = issuedAt.plusSeconds(expiresInSeconds - skewSeconds);
        return Instant.now().isAfter(expiryWithSkew);
    }

    public String authorizationHeaderValue() {
        String scheme = (tokenType == null || tokenType.isBlank()) ? "Bearer" : tokenType;
        return scheme + " " + accessToken;
    }
}
