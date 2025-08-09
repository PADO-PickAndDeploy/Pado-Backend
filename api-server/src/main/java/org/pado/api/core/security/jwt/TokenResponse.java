package org.pado.api.core.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
}