package com.zhekai.photoapp.api.users.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import com.zhekai.photoapp.api.users.service.UsersService;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableMethodSecurity(prePostEnabled=true)
@Configuration
@EnableWebSecurity
public class WebSecurity {

    private Environment environment;
    private UsersService usersService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public WebSecurity(Environment environment, UsersService usersService,
	    BCryptPasswordEncoder bCryptPasswordEncoder) {
	this.environment = environment;
	this.usersService = usersService;
	this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
	// *Deprecated method*
//	http.csrf().disable();
//	http.authorizeHttpRequests().requestMatchers(HttpMethod.POST, "/users").permitAll()
//		.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
//		.requestMatchers(HttpMethod.GET, "/users/status/check").permitAll().and().sessionManagement()
//		.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//	http.headers().frameOptions().disable();

	logger.debug("Security filterchain before");

//	Configure AuthenticationManagerBuilder
	AuthenticationManagerBuilder authenticationManagerBuilder = http
		.getSharedObject(AuthenticationManagerBuilder.class);

	authenticationManagerBuilder.userDetailsService(usersService).passwordEncoder(bCryptPasswordEncoder);

	AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

//	Create AuthenticationFilter
	AuthenticationFilter authenticationFilter = new AuthenticationFilter(usersService, environment,
		authenticationManager);
	authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));

//	for env refer to application.properties file
//	http.csrf(csrf -> csrf.disable())
//		.authorizeHttpRequests(authz -> authz.requestMatchers(HttpMethod.GET, "/actuator/**").permitAll())
//		.authorizeHttpRequests(authz -> authz.requestMatchers(HttpMethod.POST, "/users")
//			.access(new WebExpressionAuthorizationManager(
//				"hasIpAddress('" + environment.getProperty("gateway.ip") + "')"))
//			.requestMatchers("/h2-console/**", "*").permitAll()
//			.requestMatchers(HttpMethod.GET, "/users/status/check", "/users/*")
//			.access(new WebExpressionAuthorizationManager(
//				"hasIpAddress('" + environment.getProperty("gateway.ip") + "')"))
//			.anyRequest().authenticated())
//		.addFilter(authenticationFilter).authenticationManager(authenticationManager)
//		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

	http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(authz -> authz

		// Permit all GET requests to /actuator/**
		.requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()

		// Permit all requests to /h2-console/**
		.requestMatchers("/h2-console/**", "/error").permitAll()

		// Allow POST request to /users only from a specific IP
		// .requestMatchers(HttpMethod.POST, "/users")
		// .access(new WebExpressionAuthorizationManager(
		// "hasIpAddress('" + environment.getProperty("gateway.ip") + "')"))
		.requestMatchers(HttpMethod.POST, "/users").permitAll()

		// Allow GET request to /users/status/check and /users/* only from a specific IP
		// .requestMatchers(HttpMethod.GET, "/users/status/check", "/users/*")
		// .access(new WebExpressionAuthorizationManager(
		// "hasIpAddress('" + environment.getProperty("gateway.ip") + "')"))
		.requestMatchers(HttpMethod.GET, "/users/status/check", "/users/*").permitAll()

		// Require authentication for any other requests
		.anyRequest().authenticated()

	).addFilter(new AuthorizationFilter(authenticationManager, environment)).addFilter(authenticationFilter)
		.authenticationManager(authenticationManager)
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

	// Allow H2 console frame
	// *Deprecated
	// http.headers(headers -> headers.frameOptions().sameOrigin());
	http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
	logger.debug("Security filterchain after");

	return http.build();
    }
}
