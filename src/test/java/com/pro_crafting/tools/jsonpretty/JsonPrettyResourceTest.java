package com.pro_crafting.tools.jsonpretty;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@QuarkusTest
class JsonPrettyResourceTest {
    @Test
    public void testPrettify() throws IOException, URISyntaxException {
        String minified = Files.readString(Path.of(getClass().getClassLoader().getResource("minified.txt").toURI()));
        minified = "json="+minified;

        String pretty = Files.readString(Path.of(getClass().getClassLoader().getResource("pretty.txt").toURI()));

        Response response = given()
                .when().body(minified).post("/jsonpretty");

        assertEquals(200, response.statusCode());
        assertEquals(pretty, response.body().print());
    }
}