package dev.apma.cnat.apigateway.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtHelper {

    @Autowired
    private RSAPrivateKey privateKey;

    @Autowired
    private RSAPublicKey publicKey;

    public String createJwtForClaims(String subject, Map<String, String> claims) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Instant.now().toEpochMilli());
        calendar.add(Calendar.DATE, 1);

        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(subject);

        // Add claims
        claims.forEach(jwtBuilder::withClaim);

        // Add expiredAt and etc
        return jwtBuilder.withNotBefore(new Date())
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }
}