package com.zhekai.photoapp.api.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.HttpHeaders;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    @Autowired
    Environment env;

    public AuthorizationHeaderFilter() {
	super(Config.class);
    }

    public static class Config {
	// Put Configuration properties here
	private List<String> authorities;

	public List<String> getAuthorities() {
	    return authorities;
	}

	public void setAuthorities(String authorities) {
	    this.authorities = Arrays.asList(authorities.split(" "));
	}

	/*
	 * private String authority; private String role;
	 * 
	 * public String getRole() { return role; }
	 * 
	 * public void setRole(String role) { this.role = role; }
	 * 
	 * public String getAuthority() { return authority; }
	 * 
	 * public void setAuthority(String authority) { this.authority = authority; }
	 */

    }

    @Override
    public List<String> shortcutFieldOrder() {
	/* return Arrays.asList("role", "authority"); */
	return Arrays.asList("authorities");
    }

    @Override
    public GatewayFilter apply(Config config) {
	return (exchange, chain) -> {

	    ServerHttpRequest request = exchange.getRequest();

	    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
		return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
	    }

	    String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
	    String jwt = authorizationHeader.replace("Bearer ", "");

	    List<String> authorities = getAuthorities(jwt);

	    boolean hasRequiredAuthority = authorities.stream()
		    .anyMatch(authority -> config.getAuthorities().contains(authority));

	    if (!hasRequiredAuthority)
		return onError(exchange, "User is not authorized to perform this operation.", HttpStatus.FORBIDDEN);

	    /*
	     * String role = config.getRole(); String authority = config.getAuthority();
	     */

	    /*
	     * if (!isJwtValid(jwt)) { return onError(exchange, "JWT token is not valid",
	     * HttpStatus.UNAUTHORIZED); }
	     */

	    return chain.filter(exchange);
	};
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
	ServerHttpResponse response = exchange.getResponse();
	response.setStatusCode(httpStatus);
	
	DataBufferFactory bufferFactory = response.bufferFactory();
	DataBuffer dataBuffer = bufferFactory.wrap(err.getBytes());
	
	return response.writeWith(Mono.just(dataBuffer));
	/*
	 * return response.setComplete();
	 */    }

    private List<String> getAuthorities(String jwt) {
	List<String> returnValue = new ArrayList<>();

	// String subject = null;
	String tokenSecret = env.getProperty("token.secret");
	byte[] secretKeyBytes = tokenSecret.getBytes();
	SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

	JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();

	try {
	    /*
	     * Claims claims = jwtParser.parseSignedClaims(jwt).getPayload(); subject =
	     * claims.getSubject();
	     */
	    Claims claims = jwtParser.parseSignedClaims(jwt).getPayload();
	    @SuppressWarnings("unchecked")
	    List<Map<String, String>> scopes = ((Claims) claims).get("scope", List.class);
	    scopes.stream().map(scopeMap -> returnValue.add(scopeMap.get("authority"))).collect(Collectors.toList());

	} catch (Exception e) {
	    // returnValue = false;
	    return returnValue;
	}

	/*
	 * if (subject == null || subject.isEmpty()) { returnValue = false; }
	 */

	return returnValue;
    }

}
