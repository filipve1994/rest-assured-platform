package com.framework.examples.reqres;

import com.framework.base.BaseApiTest;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * IMPORTANT: reqres.in now requires a free project API key on every request.
 * Get one at https://reqres.in/signup and export it before running this class:
 *   export REQRES_API_KEY=your-key-here
 *
 * The whole class is skipped automatically (not failed) if the env var isn't set.
 */
@Epic("Public API examples")
@Feature("reqres.in")
@Tag("public-api")
@Tag("reqres")
@EnabledIfEnvironmentVariable(named = "REQRES_API_KEY", matches = ".+")
class ReqresApiTest extends BaseApiTest {

    @Inject
    private ReqresSteps steps;

    @Inject
    private ReqresScenario scenario;

    @Test
    @DisplayName("Listing users page 1 returns paginated results with expected fields")
    void listUsers_page1_returnsPaginatedResults() {
        Response response = steps.listUsers(1);
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertFieldEquals(response, "page", 1);
        ValidationUtils.assertFieldExists(response, "data[0].email");
    }

    @Test
    @DisplayName("Fetching a single known user returns their details")
    void getUser_knownId_returnsUserDetails() {
        Response response = steps.getUser(2);
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertFieldExists(response, "data.email");
    }

    @Test
    @DisplayName("Fetching a user id outside the seed range returns 404")
    void getUser_unknownId_returns404() {
        Response response = steps.getUser(9999);
        ValidationUtils.assertStatusCode(response, 404);
    }

    @Test
    @DisplayName("Full lifecycle: create -> update -> delete a user")
    void userLifecycle_createUpdateDelete() {
        scenario.createUpdateDeleteUser();
    }

    @Test
    @DisplayName("Register then log in with a fresh account")
    void account_registerThenLogin() {
        scenario.registerThenLogin();
    }
}
