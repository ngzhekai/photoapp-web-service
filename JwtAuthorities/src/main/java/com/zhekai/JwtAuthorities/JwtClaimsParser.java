package com.zhekai.JwtAuthorities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtClaimsParser {

    Jwt<?, ?> jwtObject;

    public JwtClaimsParser(String jwt, String secretToken) {
	this.jwtObject = parseJwt(jwt, secretToken);
    }

    Jwt<?, ?> parseJwt(String jwtString, String secretToken) {
	byte[] secretKeyBytes = secretToken.getBytes();
	SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

	JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();

	return jwtParser.parse(jwtString);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<? extends GrantedAuthority> getUserAuthorities() {
	Collection<Map<String, String>> scopes = ((Claims) jwtObject.getPayload()).get("scope", List.class);

	return scopes.stream().map(scopeMap -> new SimpleGrantedAuthority(scopeMap.get("authority")))
		.collect(Collectors.toList());

//	--- ChatGpt recommended method (Type-safe Conversion) ---
//	List<?> scopesRaw = ((Claims) jwtObject.getPayload()).get("scope", List.class);
//
//	if (scopesRaw != null && scopesRaw.stream().allMatch(item -> item instanceof Map)) {
//	    Collection<Map<String, String>> scopes = (Collection<Map<String, String>>) scopesRaw;
//	    return scopes.stream().map(scopeMap -> new SimpleGrantedAuthority(scopeMap.get("authority")))
//		    .collect(Collectors.toList());
//	}
//
//	throw new IllegalArgumentException("Invalid scope format in JWT claims.");
    }

    public String getJwtSubject() {
	return ((Claims) jwtObject.getPayload()).getSubject();
    }

}
