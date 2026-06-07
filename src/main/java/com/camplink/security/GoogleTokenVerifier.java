package com.camplink.security;

import com.camplink.exception.AppException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies Google ID tokens sent by the mobile app.
 *
 * <p>The verifier checks the token signature against Google's published public
 * keys, the expiry, the issuer ({@code accounts.google.com}) and — crucially —
 * that the {@code aud} (audience) claim matches one of our configured OAuth
 * client IDs. The accepted client IDs come from {@code google.client-ids} and
 * MUST include the Web client ID used as {@code serverClientId} in the Flutter
 * app, because Android's serverClientId flow mints tokens addressed to the web
 * client.
 */
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-ids}") String clientIds) {
        List<String> audiences = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        this.verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(audiences)
                .build();
    }

    /**
     * Verifies the token and returns its payload, or throws a 400 if the token
     * is missing, malformed, expired, or addressed to the wrong audience.
     */
    public GoogleIdToken.Payload verify(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw AppException.badRequest("Missing Google ID token");
        }
        GoogleIdToken token;
        try {
            token = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw AppException.badRequest("Could not verify Google token");
        }
        if (token == null) {
            // Signature/expiry/issuer/audience check failed.
            throw AppException.badRequest("Invalid or expired Google token");
        }
        return token.getPayload();
    }
}
