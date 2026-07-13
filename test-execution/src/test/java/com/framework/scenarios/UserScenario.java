package com.framework.scenarios;

import com.framework.model.User;
import com.framework.steps.UserSteps;
import com.framework.utils.RandomDataUtils;
import com.framework.utils.ValidationUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jakarta.inject.Inject;

/**
 * Scenarios compose multiple Steps into a reusable business flow (e.g.
 * "create a user and confirm it can be fetched back"). Constructor-injected
 * with the Steps class it needs - Guice builds the whole chain
 * (UserScenario -> UserSteps -> HttpClientFactory -> EnvironmentConfig/
 * AuthenticationManager) automatically.
 */
public class UserScenario {

    private final UserSteps userSteps;

    @Inject
    public UserScenario(UserSteps userSteps) {
        this.userSteps = userSteps;
    }

    @Step("Scenario: create a random user and verify it was persisted")
    public String createAndVerifyUser(String authRole) {
        User newUser = User.builder()
                .firstName(RandomDataUtils.firstName())
                .lastName(RandomDataUtils.lastName())
                .email(RandomDataUtils.uniqueEmail())
                .phoneNumber(RandomDataUtils.phoneNumber())
                .build();

        Response createResponse = userSteps.createUser(newUser, authRole);
        ValidationUtils.assertStatusCode(createResponse, 201);
        String userId = createResponse.jsonPath().getString("id");
        ValidationUtils.requireNonBlank(userId, "id");

        Response getResponse = userSteps.getUser(userId, authRole);
        ValidationUtils.assertStatusCode(getResponse, 200);
        ValidationUtils.assertFieldEquals(getResponse, "email", newUser.getEmail());

        return userId;
    }

    @Step("Scenario: create then delete a user and verify deletion")
    public void createThenDeleteUser(String authRole) {
        String userId = createAndVerifyUser(authRole);

        Response deleteResponse = userSteps.deleteUser(userId, authRole);
        ValidationUtils.assertStatusCode(deleteResponse, 204);

        Response getAfterDelete = userSteps.getUser(userId, authRole);
        ValidationUtils.assertStatusCode(getAfterDelete, 404);
    }
}
