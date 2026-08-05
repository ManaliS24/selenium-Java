package com.portfolio.automation.api;

import com.portfolio.automation.config.Configuration;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public final class ApiClient {
    private final RequestSpecification requestSpecification;

    public ApiClient() {
        int timeoutMillis = timeoutMillis();
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMillis)
                        .setParam("http.socket.timeout", timeoutMillis)
                        .setParam("http.connection-manager.timeout", (long) timeoutMillis));

        requestSpecification = RestAssured.given()
                .baseUri(Configuration.get("API_BASE_URL"))
                .accept(ContentType.JSON)
                .config(config);
    }

    public Response get(String path) {
        return RestAssured.given().spec(requestSpecification).get(path);
    }

    public Response get(String path, Map<String, ?> queryParameters) {
        return RestAssured.given().spec(requestSpecification).queryParams(queryParameters).get(path);
    }

    public Response post(String path, Object body) {
        return RestAssured.given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .body(body)
                .post(path);
    }

    private static int timeoutMillis() {
        int seconds = Configuration.getInt("SELENIUM_TIMEOUT");
        if (seconds <= 0) {
            throw new IllegalArgumentException("SELENIUM_TIMEOUT must be greater than zero");
        }
        return Math.multiplyExact(seconds, 1_000);
    }
}
