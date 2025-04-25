package com.example.demo.service.impl;

import com.example.demo.model.CustomUserDetail;
import com.example.demo.repository.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Strings;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private SecretKey secretKey;
    private long timeExpiration;
    private UserRepo userRepo;

    @Autowired
    public JwtService(
            UserRepo userRepo,
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${jwt.expiration}") long timeExpiration) {
        this.userRepo = userRepo;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.timeExpiration = timeExpiration;
    }

    public Claims buildClaims(int userId, String email, Collection<String> c) {
        Claims claims = Jwts.claims();
        claims.put("typeToken","Bearer");
        claims.put("userId",userId);
        claims.put("email",email);
        claims.put("roles",c);
        return claims;
    }
    public String generateToken(CustomUserDetail customUserDetail) {
        Date now = new Date();
        Date expirationDate = new Date(now.getDate() + timeExpiration);

        Set<String> roles = customUserDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        Claims claims = buildClaims((int) customUserDetail.getUserId(),customUserDetail.getUsername(),roles);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .setClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isValidToken(String token) {
        token = Strings.replace(token,"Bearer ","");
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public int getIdFromToken(String token) {
        if(!StringUtils.hasLength(token)) {
            throw new IllegalArgumentException("Jwt is empty or null");
        }
        token = Strings.replace(token,"Bearer ","");
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get("userId",Integer.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
