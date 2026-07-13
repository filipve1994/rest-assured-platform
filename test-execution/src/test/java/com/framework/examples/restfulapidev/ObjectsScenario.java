package com.framework.examples.restfulapidev;

import com.framework.utils.RandomDataUtils;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

import java.util.Map;

public class ObjectsScenario {

    private final ObjectSteps steps;

    @Inject
    public ObjectsScenario(ObjectSteps steps) {
        this.steps = steps;
    }

    @Step("Scenario: full object lifecycle on restful-api.dev")
    public void createUpdatePatchDeleteLifecycle() {
        ApiObject created = createAndVerifyObject();
        String id = created.getId();

        ApiObject replacement = ApiObject.builder()
                .name(created.getName() + " (renewed)")
                .data(Map.of("color", "black", "capacity", "256 GB"))
                .build();
        Response putResponse = steps.updateObject(id, replacement);
        ValidationUtils.assertStatusCode(putResponse, 200);
        ValidationUtils.assertFieldEquals(putResponse, "name", replacement.getName());

        Response patchResponse = steps.patchObject(id, Map.of("name", created.getName() + " (patched)"));
        ValidationUtils.assertStatusCode(patchResponse, 200);
        ValidationUtils.assertFieldEquals(patchResponse, "name", created.getName() + " (patched)");

        Response deleteResponse = steps.deleteObject(id);
        ValidationUtils.assertStatusCode(deleteResponse, 200);
    }

    @Step("Scenario: create a random object and verify it comes back on GET")
    public ApiObject createAndVerifyObject() {
        ApiObject newObject = ApiObject.builder()
                .name(RandomDataUtils.companyName() + " Phone")
                .data(Map.of(
                        "year", RandomDataUtils.intBetween(2020, 2026),
                        "price", RandomDataUtils.intBetween(199, 1499),
                        "cpu model", "Snapdragon 8 Gen " + RandomDataUtils.intBetween(1, 4)
                ))
                .build();

        Response createResponse = steps.createObject(newObject);
        ValidationUtils.assertStatusCode(createResponse, 200);
        String id = createResponse.jsonPath().getString("id");
        ValidationUtils.requireNonBlank(id, "id");

        Response getResponse = steps.getObject(id);
        ValidationUtils.assertStatusCode(getResponse, 200);
        ValidationUtils.assertFieldEquals(getResponse, "name", newObject.getName());

        return getResponse.as(ApiObject.class);
    }
}
