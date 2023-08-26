package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.exception.JwtRoleMismatchException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

public interface JwtHelper {
    String SUBJECT_ATTRIBUTE = "sub";
    String ROLE_ATTRIBUTE = "role";

    enum Role {
        TRACKER("tracker"),
        USER("user");

        private final String text;

        Role(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    String createJwtForClaims(String subject, Map<String, String> claims);

    String createJwtForClaimsWithNoExpiry(String subject, Map<String, String> claims);

    static String getSubjectForRole(Authentication auth, JwtHelperImpl.Role role) throws JwtRoleMismatchException {
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Map<String, Object> attributes = token.getTokenAttributes();
        String actualRole = attributes.get(JwtHelperImpl.ROLE_ATTRIBUTE).toString();
        if (role.toString().equals(actualRole)) {
            return attributes.get(JwtHelperImpl.SUBJECT_ATTRIBUTE).toString();
        }
        throw new JwtRoleMismatchException();
    }
}
