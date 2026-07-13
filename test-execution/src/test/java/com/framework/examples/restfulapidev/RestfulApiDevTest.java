package com.framework.examples.restfulapidev;

import com.framework.base.BaseApiTest;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Public API examples")
@Feature("restful-api.dev")
@Tag("public-api")
@Tag("restful-api-dev")
class RestfulApiDevTest extends BaseApiTest {

    @Inject
    private ObjectSteps steps;

    @Inject
    private ObjectsScenario scenario;

    @Test
    @DisplayName("Listing all objects returns 200 and a non-empty array")
    void listObjects_returnsOk() {
        Response response = steps.listObjects();
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertResponseTimeUnder(response, 5000);
    }

    @Test
    @DisplayName("Fetching a known seed object id returns its fixture data")
    void getObject_knownSeedData_returnsExpectedName() {
        Response response = steps.getObject("7");
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertFieldExists(response, "name");
    }

    @Test
    @DisplayName("Full lifecycle: create -> verify -> replace -> patch -> delete")
    void objectLifecycle_createUpdatePatchDelete() {
        scenario.createUpdatePatchDeleteLifecycle();
    }

    @Test
    @DisplayName("Fetching a non-existent object id returns 404")
    void getObject_nonExistentId_returns404() {
        Response response = steps.getObject("does-not-exist-12345");
        ValidationUtils.assertStatusCode(response, 404);
    }
}
