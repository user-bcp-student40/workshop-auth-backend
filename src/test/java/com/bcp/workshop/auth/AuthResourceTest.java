package com.bcp.workshop.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthResourceTest {
    @Test
    void logsInWithWorkshopUserAndReturnsSignedToken() throws Exception {
        String token = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("expiresIn", is(3600))
                .body("token", notNullValue())
                .extract().path("token");

        assertSignedToken(token);
    }

    private static void assertSignedToken(String token) throws Exception {
        String[] segments = token.split("\\.");
        assertEquals(3, segments.length);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode header = mapper.readTree(Base64.getUrlDecoder().decode(segments[0]));
        JsonNode claims = mapper.readTree(Base64.getUrlDecoder().decode(segments[1]));
        assertEquals("RS256", header.path("alg").asText());
        assertEquals("student", claims.path("sub").asText());
        assertEquals("STUDENT", claims.path("role").asText());
        assertTrue(claims.path("exp").asLong() > Instant.now().getEpochSecond());

        try (var keyStream = AuthResourceTest.class.getResourceAsStream("/test-public-key.pem")) {
            assertNotNull(keyStream);
            String encodedKey = new String(keyStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            var publicKey = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(encodedKey)));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update((segments[0] + "." + segments[1]).getBytes(StandardCharsets.US_ASCII));
            assertTrue(verifier.verify(Base64.getUrlDecoder().decode(segments[2])));
        }
    }
}
