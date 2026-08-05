package com.portfolio.automation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.portfolio.automation.api.ApiClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public final class PostsApiTest {
    private ApiClient apiClient;
    private PostContractValidator contractValidator;

    @BeforeClass(alwaysRun = true)
    public void createClient() {
        apiClient = new ApiClient();
        contractValidator = new PostContractValidator();
    }

    @Test(groups = {"api", "smoke"})
    public void getPostReturnsExpectedContract() {
        Response response = apiClient.get("/posts/1");

        assertJsonResponse(response, 200);
        JsonNode post = contractValidator.parse(response.asString());
        contractValidator.assertValid(post);
        assertRequiredFieldTypes(post);
        Assert.assertEquals(post.get("id").intValue(), 1);
    }

    @Test(groups = {"api", "regression"})
    public void postsCollectionMatchesContract() {
        Response response = apiClient.get("/posts");

        assertJsonResponse(response, 200);
        JsonNode posts = contractValidator.parse(response.asString());
        Assert.assertTrue(posts.isArray());
        Assert.assertFalse(posts.isEmpty());
        posts.forEach(post -> {
            contractValidator.assertValid(post);
            assertRequiredFieldTypes(post);
        });
    }

    @Test(groups = {"api", "regression"})
    public void postsCanBeFilteredByUser() {
        Response response = apiClient.get("/posts", Map.of("userId", 1));

        assertJsonResponse(response, 200);
        JsonNode posts = contractValidator.parse(response.asString());
        Assert.assertTrue(posts.isArray());
        Assert.assertFalse(posts.isEmpty());
        posts.forEach(post -> Assert.assertEquals(post.get("userId").intValue(), 1));
    }

    @Test(groups = {"api", "regression"})
    public void createPostEchoesPayload() {
        Map<String, Object> payload = Map.of(
                "title", "Selenium portfolio",
                "body", "API automation",
                "userId", 7);

        Response response = apiClient.post("/posts", payload);

        assertJsonResponse(response, 201);
        JsonNode created = contractValidator.parse(response.asString());
        Assert.assertEquals(created.get("title").textValue(), payload.get("title"));
        Assert.assertEquals(created.get("body").textValue(), payload.get("body"));
        Assert.assertEquals(created.get("userId").intValue(), payload.get("userId"));
        Assert.assertTrue(created.get("id").isIntegralNumber());
    }

    @Test(groups = {"api", "regression"})
    public void missingPostReturnsNotFound() {
        Response response = apiClient.get("/posts/999999");

        assertJsonResponse(response, 404);
        JsonNode body = contractValidator.parse(response.asString());
        Assert.assertTrue(body.isObject());
        Assert.assertTrue(body.isEmpty(), "Expected an empty JSON object");
    }

    private static void assertJsonResponse(Response response, int expectedStatus) {
        Assert.assertEquals(response.statusCode(), expectedStatus);
        Assert.assertTrue(response.contentType().toLowerCase().startsWith("application/json"),
                "Expected a JSON content type but received: " + response.contentType());
    }

    private static void assertRequiredFieldTypes(JsonNode post) {
        Assert.assertTrue(post.has("userId") && post.get("userId").isIntegralNumber());
        Assert.assertTrue(post.has("id") && post.get("id").isIntegralNumber());
        Assert.assertTrue(post.has("title") && post.get("title").isTextual());
        Assert.assertTrue(post.has("body") && post.get("body").isTextual());
    }
}
