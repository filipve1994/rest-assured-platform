package com.framework.steps;

import com.framework.http.HttpClientFactory;
import com.framework.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

/**
 * One "step" class per resource/domain area. Steps wrap a single API call
 * and return the raw Response, so both scenarios (which chain steps together)
 * and individual tests (which want fine-grained control) can use them.
 *
 * Constructor-injected with HttpClientFactory rather than reaching for a
 * static accessor - Guice resolves this automatically wherever a UserSteps
 * is @Inject-ed (into a Scenario, or directly into a test's @Inject field).
 */
public class UserSteps {

    private final HttpClientFactory http;

    @Inject
    public UserSteps(HttpClientFactory http) {
        this.http = http;
    }

    @Step("Create user with payload: {user}")
    public Response createUser(User user, String authRole) {
        return http.spec().withAuth(authRole).request()
                .body(user)
                .when()
                .post("/users");
    }

    @Step("Get user by id: {userId}")
    public Response getUser(String userId, String authRole) {
        return http.spec().withAuth(authRole).request()
                .when()
                .get("/users/{id}", userId);
    }

    @Step("Update user {userId} with payload: {user}")
    public Response updateUser(String userId, User user, String authRole) {
        return http.spec().withAuth(authRole).request()
                .body(user)
                .when()
                .put("/users/{id}", userId);
    }

    @Step("Delete user: {userId}")
    public Response deleteUser(String userId, String authRole) {
        return http.spec().withAuth(authRole).request()
                .when()
                .delete("/users/{id}", userId);
    }

    @Step("List users (page={page}, size={size})")
    public Response listUsers(int page, int size, String authRole) {
        return http.spec().withAuth(authRole).request()
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get("/users");
    }
}
