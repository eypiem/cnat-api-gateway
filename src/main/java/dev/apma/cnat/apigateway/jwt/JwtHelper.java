package dev.apma.cnat.apigateway.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class JwtHelper {
    public static final String SUBJECT_ATTRIBUTE = "sub";
    public static final String ROLE_ATTRIBUTE = "role";

    public enum Role {
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

    @Autowired
    private RSAPrivateKey privateKey;

    @Autowired
    private RSAPublicKey publicKey;

    public String createJwtForClaims(String subject, Map<String, String> claims) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Instant.now().toEpochMilli());
        calendar.add(Calendar.DATE, 1);

        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(subject);
        if (claims != null) {
            claims.forEach(jwtBuilder::withClaim);
        }

        return jwtBuilder.withNotBefore(new Date())
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public String createJwtForClaimsWithNoExpiry(String subject, Map<String, String> claims) {
        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(subject);
        claims.forEach(jwtBuilder::withClaim);

        return jwtBuilder.withNotBefore(new Date()).sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public static <T> T onRoleMatch(JwtAuthenticationToken token, Role role, Supplier<T> onMatch, Supplier<T> orElse) {
        Map<String, Object> attributes = token.getTokenAttributes();
        String actualRole = attributes.get(JwtHelper.ROLE_ATTRIBUTE).toString();
        if (role.toString().equals(actualRole)) {
            return onMatch.get();
        } else {
            return orElse.get();
        }
    }

    public static String getSubject(JwtAuthenticationToken token) {
        Map<String, Object> attributes = token.getTokenAttributes();
        return attributes.get(JwtHelper.SUBJECT_ATTRIBUTE).toString();
    }
}
