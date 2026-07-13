package com.framework.examples.jsonplaceholder;

import com.framework.config.EnvironmentConfig;
import com.framework.http.HttpClientFactory;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

import java.util.Map;

/**
 * https://jsonplaceholder.typicode.com - free, no-auth fake REST API.
 * IMPORTANT: writes (POST/PUT/PATCH/DELETE) are simulated only - nothing is
 * actually persisted server-side.
 */
public class PostSteps {

    private final HttpClientFactory http;
    private final EnvironmentConfig config;

    @Inject
    public PostSteps(HttpClientFactory http, EnvironmentConfig config) {
        this.http = http;
        this.config = config;
    }

    private String baseUrl() {
        return config.text("publicApis.jsonPlaceholder.baseUrl");
    }

    @Step("[jsonplaceholder] List all posts")
    public Response listPosts() {
        return http.spec(baseUrl()).request()
                .when()
                .get("/posts");
    }

    @Step("[jsonplaceholder] List posts for user: {userId}")
    public Response listPostsForUser(int userId) {
        return http.spec(baseUrl()).request()
                .queryParam("userId", userId)
                .when()
                .get("/posts");
    }

    @Step("[jsonplaceholder] Get post by id: {id}")
    public Response getPost(int id) {
        return http.spec(baseUrl()).request()
                .when()
                .get("/posts/{id}", id);
    }

    @Step("[jsonplaceholder] Get comments for post: {postId}")
    public Response getCommentsForPost(int postId) {
        return http.spec(baseUrl()).request()
                .when()
                .get("/posts/{id}/comments", postId);
    }

    @Step("[jsonplaceholder] Create post (simulated, not persisted): {post}")
    public Response createPost(Post post) {
        return http.spec(baseUrl()).request()
                .body(post)
                .when()
                .post("/posts");
    }

    @Step("[jsonplaceholder] Replace post {id} (simulated, not persisted): {post}")
    public Response updatePost(int id, Post post) {
        return http.spec(baseUrl()).request()
                .body(post)
                .when()
                .put("/posts/{id}", id);
    }

    @Step("[jsonplaceholder] Partially update post {id} (simulated, not persisted): {fields}")
    public Response patchPost(int id, Map<String, Object> fields) {
        return http.spec(baseUrl()).request()
                .body(fields)
                .when()
                .patch("/posts/{id}", id);
    }

    @Step("[jsonplaceholder] Delete post (simulated, not persisted): {id}")
    public Response deletePost(int id) {
        return http.spec(baseUrl()).request()
                .when()
                .delete("/posts/{id}", id);
    }
}
