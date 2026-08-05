package com.portfolio.automation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

final class PostContractValidator {
    private static final String SCHEMA_RESOURCE = "schemas/post.schema.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchema schema;

    PostContractValidator() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Schema resource not found: " + SCHEMA_RESOURCE);
            }
            schema = factory.getSchema(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load schema: " + SCHEMA_RESOURCE, exception);
        }
    }

    JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException exception) {
            throw new AssertionError("Response is not valid JSON", exception);
        }
    }

    void assertValid(JsonNode post) {
        Set<ValidationMessage> errors = schema.validate(post);
        String details = errors.stream()
                .map(ValidationMessage::getMessage)
                .sorted()
                .collect(Collectors.joining("\n"));
        Assert.assertTrue(errors.isEmpty(), "Post contract violations:\n" + details);
    }
}
