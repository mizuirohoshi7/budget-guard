package com.budgetguard.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.budgetguard.global.config.security.filter.JwtFilter;
import com.budgetguard.global.config.security.handler.CustomAccessDeniedHandler;
import com.budgetguard.global.config.security.handler.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final TokenManager tokenManager;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)

			// 기본 세션을 사용하지 않도록 설정
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(request ->
				request.requestMatchers(
					"/api/v1/auth/**"
				).permitAll())
			.authorizeHttpRequests(request ->
				request.anyRequest().authenticated())

			.exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler)) // 권한 없이 접근
			.exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint)) // 유효하지 않은 자격으로 접근

			// 요청의 헤더에 유효한 JWT 토큰이 있으면 SecurityContext 에 저장하는 필터
			.addFilterBefore(new JwtFilter(tokenManager), UsernamePasswordAuthenticationFilter.class)

			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
