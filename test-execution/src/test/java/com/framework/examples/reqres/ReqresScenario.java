package com.framework.examples.reqres;

import com.framework.utils.RandomDataUtils;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

public class ReqresScenario {

    private final ReqresSteps steps;

    @Inject
    public ReqresScenario(ReqresSteps steps) {
        this.steps = steps;
    }

    @Step("Scenario: create, update then delete a reqres user")
    public void createUpdateDeleteUser() {
        String name = RandomDataUtils.fullName();
        String job = "QA Engineer";

        Response createResponse = steps.createUser(name, job);
        ValidationUtils.assertStatusCode(createResponse, 201);
        ValidationUtils.assertFieldEquals(createResponse, "name", name);
        String userId = createResponse.jsonPath().getString("id");
        ValidationUtils.requireNonBlank(userId, "id");

        Response updateResponse = steps.updateUser(Integer.parseInt(userId), name, "Senior QA Engineer");
        ValidationUtils.assertStatusCode(updateResponse, 200);
        ValidationUtils.assertFieldEquals(updateResponse, "job", "Senior QA Engineer");

        Response deleteResponse = steps.deleteUser(Integer.parseInt(userId));
        ValidationUtils.assertStatusCode(deleteResponse, 204);
    }

    @Step("Scenario: register then log in with a fresh account")
    public void registerThenLogin() {
        String email = RandomDataUtils.uniqueEmail();
        String password = "Sup3rSecret!" + RandomDataUtils.alphanumeric(4);

        Response registerResponse = steps.register(email, password);
        ValidationUtils.assertStatusCode(registerResponse, 200);
        ValidationUtils.assertFieldExists(registerResponse, "token");

        Response loginResponse = steps.login(email, password);
        ValidationUtils.assertStatusCode(loginResponse, 200);
        ValidationUtils.assertFieldExists(loginResponse, "token");
    }
}
