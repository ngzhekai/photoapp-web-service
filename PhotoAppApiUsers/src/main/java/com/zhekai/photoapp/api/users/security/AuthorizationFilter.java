package com.zhekai.photoapp.api.users.security;

import java.io.IOException;
//import java.util.ArrayList;

//import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.zhekai.JwtAuthorities.JwtClaimsParser;

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    private Environment environment;

    public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
	super(authenticationManager);
	this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
	    throws IOException, ServletException {

	String authorizationHeader = req.getHeader(environment.getProperty("authorization.token.header.name"));

	if (authorizationHeader == null
		|| !authorizationHeader.startsWith(environment.getProperty("authorization.token.header.prefix"))) {
	    chain.doFilter(req, res);
	    return;
	}

	UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

	SecurityContextHolder.getContext().setAuthentication(authentication);
	chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest req) {
	String authorizationHeader = req.getHeader(environment.getProperty("authorization.token.header.name"));

	if (authorizationHeader == null) {
	    return null;
	}

//	String userId = null;
	String token = authorizationHeader.replace(environment.getProperty("authorization.token.header.prefix"), "");
	String tokenSecret = environment.getProperty("token.secret");

	if (tokenSecret == null)
	    return null;

//	byte[] secretKeyBytes = tokenSecret.getBytes();
//	SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
//
//	JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
//	try {
//	    Claims claims = jwtParser.parseSignedClaims(token).getPayload();
//	    userId = claims.getSubject();
//	} catch (Exception e) {
//	    return null;
//	}
	
	JwtClaimsParser jwtClaimsParser = new JwtClaimsParser(token, tokenSecret);
	String userId = jwtClaimsParser.getJwtSubject();


	if (userId == null)
	    return null;

	return new UsernamePasswordAuthenticationToken(userId, null, jwtClaimsParser.getUserAuthorities());
    }

}
