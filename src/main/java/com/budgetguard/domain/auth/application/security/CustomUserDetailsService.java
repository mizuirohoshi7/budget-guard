package com.budgetguard.domain.auth.application.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * CustomAuthenticationProvider에서 사용할 UserDetails를 생성 및 예외 처리한다.
 */
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findByAccount(username).orElseThrow(
			() -> new BusinessException(username, "account", ErrorCode.MEMBER_ACCOUNT_NOT_FOUND)
		);
		return createUserDetails(member);
	}

	private UserDetails createUserDetails(Member member) {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().toString()); // 권한 정보 추출
		return new User(member.getAccount(), member.getPassword(), Collections.singleton(authority));
	}
}
