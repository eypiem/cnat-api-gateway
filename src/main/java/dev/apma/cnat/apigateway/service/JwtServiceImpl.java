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

/**
 * An implementation of the {@code JwtService} interface.
 *
 * @author Amir Parsa Mahdian
 * @see dev.apma.cnat.apigateway.service.JwtService
 */
@Service
public class JwtServiceImpl implements JwtService {

    /**
     * The RSA public key used for signing JWTs
     */
    private final RSAPublicKey publicKey;

    /**
     * The RSA private key used for signing JWTs
     */
    private final RSAPrivateKey privateKey;

    @Autowired
    public JwtServiceImpl(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
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

    @Override
    public String createJwtForClaimsWithNoExpiry(String subject, Map<String, String> claims) {
        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(subject);
        claims.forEach(jwtBuilder::withClaim);

        return jwtBuilder.withNotBefore(new Date()).sign(Algorithm.RSA256(publicKey, privateKey));
    }
}
