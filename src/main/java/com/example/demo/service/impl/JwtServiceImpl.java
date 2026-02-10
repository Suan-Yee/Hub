package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final String secret = "fjakfheuivnslWH232rfjnlet4209rfdf";

    private final Supplier<SecretKey> key = () -> Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

    private final Function<String, Claims> extractClaims = token ->
            Jwts.parser()
                    .verifyWith(key.get())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

    private final Function<String, String> extractSubject = token -> getClaimsValue(token,Claims::getSubject);

    private final <T> T getClaimsValue(String token, Function<Claims, T> claimsResolver) {
        Claims claim = extractClaims.apply(token);
        return claimsResolver.apply(claim);
    }

    private final Supplier<JwtBuilder> builder = () ->
            Jwts.builder()
                    .header().add(Map.of("TYPE", "JWT_TYPE"))
                    .and()
                    .audience().add("SUAN")
                    .and()
                    .id(UUID.randomUUID().toString())
                    .issuedAt(Date.from(Instant.now()))
                    .notBefore(new Date())
                    .signWith(key.get());

    private final Function<User, String> buildToken = user ->
            builder.get()
                    .subject(user.getEmail())
                    .claim("role","admin")
                    .expiration(Date.from(Instant.now().plusSeconds(343434)))
                    .compact();
}
