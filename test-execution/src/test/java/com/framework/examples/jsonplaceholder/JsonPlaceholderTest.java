package com.framework.examples.jsonplaceholder;

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
@Feature("JSONPlaceholder")
@Tag("public-api")
@Tag("jsonplaceholder")
class JsonPlaceholderTest extends BaseApiTest {

    @Inject
    private PostSteps steps;

    @Inject
    private JsonPlaceholderScenario scenario;

    @Test
    @DisplayName("Listing all posts returns the fixed set of 100 seed posts")
    void listPosts_returnsAllSeedPosts() {
        Response response = steps.listPosts();
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertListSize(response, "", 100);
    }

    @Test
    @DisplayName("Filtering posts by userId returns only that user's posts")
    void listPostsForUser_returnsOnlyThatUsersPosts() {
        Response response = steps.listPostsForUser(1);
        ValidationUtils.assertStatusCode(response, 200);
        java.util.List<Integer> userIds = response.jsonPath().getList("userId", Integer.class);
        org.assertj.core.api.Assertions.assertThat(userIds).allMatch(id -> id == 1);
    }

    @Test
    @DisplayName("Fetching a known seed post returns its fixed content")
    void getPost_knownId_returnsExpectedPost() {
        Response response = steps.getPost(1);
        ValidationUtils.assertStatusCode(response, 200);
        ValidationUtils.assertFieldEquals(response, "id", 1);
        ValidationUtils.assertFieldExists(response, "title");
    }

    @Test
    @DisplayName("Fetching a post id beyond the fixed dataset (>100) returns 404")
    void getPost_idOutsideSeedRange_returns404() {
        Response response = steps.getPost(9999);
        ValidationUtils.assertStatusCode(response, 404);
    }

    @Test
    @DisplayName("Reading a post with its comments, then simulating a new post creation")
    void readPostAndComments_thenSimulateCreate() {
        scenario.readPostWithCommentsThenSimulateCreate();
    }
}
