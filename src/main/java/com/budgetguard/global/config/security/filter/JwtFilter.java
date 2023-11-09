package com.budgetguard.global.config.security.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.budgetguard.global.config.security.TokenManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final int BEARER_LENGTH = 7;

	private final TokenManager tokenManager;

	/**
	 * JWT 토큰의 인증 정보를 현재 스레드의 SecurityContext 에 저장한다.
	 */
	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// 헤더에서 토큰 정보를 추출한다.
		String token = resolveToken(request);

		// 토큰이 존재하고, 유효한 경우 SecurityContext 에 저장한다.
		if (StringUtils.hasText(token) && tokenManager.validateToken(token)) {
			Authentication authentication = tokenManager.createAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Request Header 에서 토큰 정보를 추출한다.
	 *
	 * @param request HttpServletRequest
	 * @return 토큰 정보
	 */
	private String resolveToken(HttpServletRequest request) {
		// 헤더에서 토큰 정보를 추출한다.
		String token = request.getHeader(AUTHORIZATION_HEADER);

		// 토큰이 존재하고, Bearer 로 시작하는 경우 토큰 정보를 반환한다.
		if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
			return token.substring(BEARER_LENGTH);
		}

		return null;
	}
}
