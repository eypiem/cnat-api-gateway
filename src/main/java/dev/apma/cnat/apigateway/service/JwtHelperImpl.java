package dev.apma.cnat.apigateway.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Service
public class JwtHelperImpl implements JwtHelper {

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    @Autowired
    public JwtHelperImpl(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

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
}
