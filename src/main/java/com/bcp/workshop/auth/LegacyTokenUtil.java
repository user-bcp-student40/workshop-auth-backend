package com.bcp.workshop.auth;

import io.smallrye.jwt.build.Jwt;

import java.time.Instant;

public final class LegacyTokenUtil {
    private LegacyTokenUtil() {}

    public static String create(String username, String role) {
        long expiry = Instant.now().plusSeconds(3600).getEpochSecond();
        return Jwt.claims()
                .subject(username)
                .claim("role", role)
                .expiresAt(expiry)
                .sign();
    }
}
