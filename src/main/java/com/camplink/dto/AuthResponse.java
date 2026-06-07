package com.camplink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
    /** True when a (typically Google) user still needs to supply phone/role. */
    private boolean needsProfileCompletion;

    /** Convenience for the password flows, where the profile is always complete. */
    public AuthResponse(String token, UserResponse user) {
        this(token, user, false);
    }
}
