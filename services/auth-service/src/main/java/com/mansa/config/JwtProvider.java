package com.mansa.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//import java.security.Key;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;


@ConfigurationProperties("jwt")
@Component
public class JwtProvider {

    //private final Key key;
    private final SecretKey skey;
    private final long validitySeconds;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-validity-seconds:900}") long validitySeconds) {
        //this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validitySeconds = validitySeconds;
        this.skey= Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String subject, String role) {
        Instant now = Instant.now();
       return Jwts.builder()
            .subject(subject)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(validitySeconds)))
            .signWith(skey, Jwts.SIG.HS256)
            .compact();
    }

        public Jws<Claims> parseToken(String token) {
           //return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        
                Jws<Claims> jws =    Jwts.parser().verifyWith(skey).build().parseSignedClaims(token);
                  //.getPayload();
                // Claims claims = jws.getPayload();
                // return (Jws<Claims>) claims;
                return jws;       
       }

        public long getValiditySeconds() {
          return validitySeconds;
         
        }

   
        
}

