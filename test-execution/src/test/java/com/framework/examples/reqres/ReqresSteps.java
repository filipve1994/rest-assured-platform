package com.framework.examples.reqres;

import com.framework.config.EnvironmentConfig;
import com.framework.http.HttpClientFactory;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;

import java.util.Map;

/**
 * https://reqres.in - note that as of 2026 EVERY request requires a project
 * "x-api-key" header (get a free one at https://reqres.in/signup and export
 * it as the REQRES_API_KEY environment variable - see application.yml).
 * Older tutorials showing reqres.in with no auth are out of date.
 */
public class ReqresSteps {

    private final HttpClientFactory http;
    private final EnvironmentConfig config;

    @Inject
    public ReqresSteps(HttpClientFactory http, EnvironmentConfig config) {
        this.http = http;
        this.config = config;
    }

    private String baseUrl() {
        return config.text("publicApis.reqres.baseUrl");
    }

    private String apiKey() {
        String key = config.text("publicApis.reqres.apiKey");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "REQRES_API_KEY is not set. Get a free key at https://reqres.in/signup "
                            + "and export it as the REQRES_API_KEY environment variable before running these tests.");
        }
        return key;
    }

    private RequestSpecification authedRequest() {
        return http.spec(baseUrl())
                .header("x-api-key", apiKey())
                .request();
    }

    @Step("[reqres] List users, page {page}")
    public Response listUsers(int page) {
        return authedRequest()
                .queryParam("page", page)
                .when()
                .get("/users");
    }

    @Step("[reqres] Get single user by id: {id}")
    public Response getUser(int id) {
        return authedRequest()
                .when()
                .get("/users/{id}", id);
    }

    @Step("[reqres] Create user: name={name}, job={job}")
    public Response createUser(String name, String job) {
        return authedRequest()
                .body(Map.of("name", name, "job", job))
                .when()
                .post("/users");
    }

    @Step("[reqres] Update user {id}: name={name}, job={job}")
    public Response updateUser(int id, String name, String job) {
        return authedRequest()
                .body(Map.of("name", name, "job", job))
                .when()
                .put("/users/{id}", id);
    }

    @Step("[reqres] Delete user: {id}")
    public Response deleteUser(int id) {
        return authedRequest()
                .when()
                .delete("/users/{id}", id);
    }

    @Step("[reqres] Register a new account: {email}")
    public Response register(String email, String password) {
        return authedRequest()
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/register");
    }

    @Step("[reqres] Login: {email}")
    public Response login(String email, String password) {
        return authedRequest()
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/login");
    }
}
