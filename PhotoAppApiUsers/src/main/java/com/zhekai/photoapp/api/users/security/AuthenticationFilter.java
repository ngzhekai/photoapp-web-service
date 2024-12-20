package com.zhekai.photoapp.api.users.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhekai.photoapp.api.users.service.UsersService;
import com.zhekai.photoapp.api.users.shared.UserDto;
import com.zhekai.photoapp.api.users.ui.model.LoginRequestModel;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UsersService usersService;
    private Environment environment;

    public AuthenticationFilter(UsersService usersService, Environment environment,
	    AuthenticationManager authenticationManager) {
	super(authenticationManager);
	this.usersService = usersService;
	this.environment = environment;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
	    throws AuthenticationException {
	try {
	    LoginRequestModel creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequestModel.class);

	    return getAuthenticationManager().authenticate(
		    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
	    Authentication auth) throws IOException, ServletException {

	String userName = ((User) auth.getPrincipal()).getUsername();
	UserDto userDetails = usersService.getUserDetailsByEmail(userName);
	String tokenSecret = environment.getProperty("token.secret");

//	Deprecated method
//	byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
//	SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

//	byte[] secretKeyBytes = Base64.getDecoder().decode(tokenSecret);
	byte[] secretKeyBytes = tokenSecret.getBytes();
	SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

	Instant now = Instant.now();
	String token = Jwts.builder().claim("scope", auth.getAuthorities()).subject(userDetails.getUserId())
		.expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration_time")))))
		.issuedAt(Date.from(now)).signWith(secretKey).compact();

	res.addHeader("token", token);
	res.addHeader("userId", userDetails.getUserId());

    }

}
