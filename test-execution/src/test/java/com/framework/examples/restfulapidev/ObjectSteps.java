package com.framework.examples.restfulapidev;

import com.framework.config.EnvironmentConfig;
import com.framework.http.HttpClientFactory;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

import java.util.List;

/**
 * https://api.restful-api.dev - a free, no-auth sandbox API supporting full
 * CRUD (GET/POST/PUT/PATCH/DELETE) over a generic "objects" resource.
 * Constructor-injected with EnvironmentConfig (to read the fixed base URL)
 * and HttpClientFactory (to build requests against it).
 */
public class ObjectSteps {

    private final HttpClientFactory http;
    private final EnvironmentConfig config;

    @Inject
    public ObjectSteps(HttpClientFactory http, EnvironmentConfig config) {
        this.http = http;
        this.config = config;
    }

    private String baseUrl() {
        return config.text("publicApis.restfulApiDev.baseUrl");
    }

    @Step("[restful-api.dev] List all objects")
    public Response listObjects() {
        return http.spec(baseUrl()).request()
                .when()
                .get("/objects");
    }

    @Step("[restful-api.dev] List objects filtered by ids: {ids}")
    public Response listObjectsByIds(List<String> ids) {
        var builder = http.spec(baseUrl());
        ids.forEach(id -> builder.queryParam("id", id));
        return builder.request()
                .when()
                .get("/objects");
    }

    @Step("[restful-api.dev] Get object by id: {id}")
    public Response getObject(String id) {
        return http.spec(baseUrl()).request()
                .when()
                .get("/objects/{id}", id);
    }

    @Step("[restful-api.dev] Create object: {object}")
    public Response createObject(ApiObject object) {
        return http.spec(baseUrl()).request()
                .body(object)
                .when()
                .post("/objects");
    }

    @Step("[restful-api.dev] Replace object {id} with: {object}")
    public Response updateObject(String id, ApiObject object) {
        return http.spec(baseUrl()).request()
                .body(object)
                .when()
                .put("/objects/{id}", id);
    }

    @Step("[restful-api.dev] Partially update object {id} with fields: {partialFields}")
    public Response patchObject(String id, Object partialFields) {
        return http.spec(baseUrl()).request()
                .body(partialFields)
                .when()
                .patch("/objects/{id}", id);
    }

    @Step("[restful-api.dev] Delete object: {id}")
    public Response deleteObject(String id) {
        return http.spec(baseUrl()).request()
                .when()
                .delete("/objects/{id}", id);
    }
}
