package com.budgetguard.domain.auth.application.security;

import static com.budgetguard.global.error.ErrorCode.*;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.budgetguard.global.error.BusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 비밀번호 검증 시, 예외 처리를 위해서 커스텀
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private final CustomUserDetailsService userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String account = authentication.getName();
		String password = (String) authentication.getCredentials();

		// 계정명으로 사용자 조회
		UserDetails member = userDetailsService.loadUserByUsername(account);

		// 비밀번호 검증
		if (!password.equals(member.getPassword())) {
			throw new BusinessException(password, "password", WRONG_PASSWORD);
		}

		// AuthService에 Authentication 반환
		return new UsernamePasswordAuthenticationToken(account, password, member.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
