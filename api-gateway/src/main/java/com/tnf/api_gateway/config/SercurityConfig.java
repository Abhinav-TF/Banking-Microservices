package com.tnf.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SercurityConfig {

	@Bean 
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				//Disable CSRF for stateless APIs
				.csrf(csrf -> csrf.disable())
				//Define which endpoints are public vs protected
				.authorizeExchange(exchange -> exchange
						//public endpoints (no authentication needed)
				.pathMatchers("/actuator/**").permitAll()
				.pathMatchers("/api/auth/**").permitAll()
				//All other endpoints require authentication
				.anyExchange().authenticated())
				
				//Enable http basic Autjentication
				.httpBasic(httpBasic -> {}).build();
	}
	
	@Bean
	public MapReactiveUserDetailsService userDetailsService() {
		//Define users
		UserDetails admin = User.builder().username("admin")
				.password(passwordEncoder().encode("admin123"))
				.roles("ADMIN","USER").build();
		
		UserDetails user = User.builder().username("user")
				.password(passwordEncoder().encode("user123"))
				.roles("USER").build();
		
		UserDetails guest = User.builder().username("guest")
				.password(passwordEncoder().encode("guest123"))
				.roles("USER").build();
		
		return new MapReactiveUserDetailsService(admin, user, guest);
	}
	
	@Bean 
	public PasswordEncoder passwordEncoder() {
		return new  BCryptPasswordEncoder();
	}
}
