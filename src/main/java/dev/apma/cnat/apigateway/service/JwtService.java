package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.exception.JwtRoleMismatchException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

/**
 * This interface represents a service for handling JWTs.
 *
 * @author Amir Parsa Mahdian
 */
public interface JwtService {
    /**
     * Subject attribute key name in JWT claims
     */
    String SUBJECT_ATTRIBUTE = "sub";

    /**
     * Role attribute key name in JWT claims
     */
    String ROLE_ATTRIBUTE = "role";

    /**
     * The role specifies if a JWT is issued for a tracker or a user
     */
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

    /**
     * Returns a new JWT with the provided subject, claim, and default expiry.
     *
     * @param subject the subject of the JWT
     * @param claims  the claims to include in the JWT
     * @return a new JWT with the provided subject, claim, and default expiry
     */
    String createJwtForClaims(String subject, Map<String, String> claims);

    /**
     * Returns a new JWT with the provided subject, claim, and no expiry.
     *
     * @param subject the subject of the JWT
     * @param claims  the claims to include in the JWT
     * @return a new JWT with the provided subject, claim, and no expiry
     */
    String createJwtForClaimsWithNoExpiry(String subject, Map<String, String> claims);

    /**
     * Returns the subject of the provided JWT if it has the same role as the provided role.
     *
     * @param auth the JWT
     * @param role the role to verify
     * @return the subject of the provided JWT if its role matches the provided role
     * @throws JwtRoleMismatchException if the JWT's role does not match the provided role
     */
    static String getSubjectForRole(Authentication auth, JwtServiceImpl.Role role) throws JwtRoleMismatchException {
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Map<String, Object> attributes = token.getTokenAttributes();
        String actualRole = attributes.get(JwtServiceImpl.ROLE_ATTRIBUTE).toString();
        if (role.toString().equals(actualRole)) {
            return attributes.get(JwtServiceImpl.SUBJECT_ATTRIBUTE).toString();
        }
        throw new JwtRoleMismatchException();
    }
}
