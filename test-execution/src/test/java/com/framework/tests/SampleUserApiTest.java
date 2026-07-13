package com.framework.tests;

import com.framework.base.BaseApiTest;
import com.framework.model.User;
import com.framework.scenarios.UserScenario;
import com.framework.steps.UserSteps;
import com.framework.utils.JsonUtils;
import com.framework.utils.RandomDataUtils;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("User Management")
@Feature("User CRUD")
class SampleUserApiTest extends BaseApiTest {

    // Injected by GuiceExtension (see BaseApiTest) right after this test
    // instance is constructed - no `new UserSteps()` anywhere.
    @Inject
    private UserSteps userSteps;

    @Inject
    private UserScenario userScenario;

    @Test
    @Tag("smoke")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Creating a user returns 201 and a matching schema")
    @Description("Loads a fixed JSON payload, POSTs it, validates status + JSON schema")
    void createUser_returnsCreatedAndValidSchema() {
        User payload = JsonUtils.loadTestData("testdata/create_user.json", User.class);
        payload.setEmail(RandomDataUtils.uniqueEmail());

        Response response = userSteps.createUser(payload, "default");

        ValidationUtils.assertStatusCode(response, 201);
        ValidationUtils.assertMatchesSchema(response, "schemas/user_schema.json");
        ValidationUtils.assertFieldEquals(response, "email", payload.getEmail());
        ValidationUtils.assertResponseTimeUnder(response, 3000);
    }

    @Test
    @Tag("regression")
    @DisplayName("Full lifecycle: create -> verify -> delete -> verify gone")
    void userLifecycle_createThenDelete() {
        userScenario.createThenDeleteUser("default");
    }

    @Test
    @Tag("regression")
    @DisplayName("Fetching a non-existent user returns 404")
    void getUser_notFound_returns404() {
        Response response = userSteps.getUser("does-not-exist-" + RandomDataUtils.uuid(), "default");
        ValidationUtils.assertStatusCode(response, 404);
    }
}
