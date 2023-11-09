package com.budgetguard.domain.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.budgetguard.domain.member.MemberTestHelper;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	static final Member member = MemberTestHelper.createMember();

	@InjectMocks
	AuthService authService;

	@Mock
	MemberRepository memberRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Nested
	@DisplayName("회원 가입")
	class signup {
		@Test
		@DisplayName("회원 가입 성공")
		void 회원_가입_성공() {
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(member.getPassword())
				.build();

			given(memberRepository.findByAccount(any())).willReturn(Optional.empty());
			given(memberRepository.save(any())).willReturn(member);

			Long memberId = authService.signup(param);

			assertThat(memberId).isEqualTo(member.getId());
		}

		@Test
		@DisplayName("이미 사용중인 계정명이면 실패")
		void 이미_사용중인_계정명이면_실패() {
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(member.getPassword())
				.build();

			given(memberRepository.findByAccount(any())).willReturn(Optional.of(member));

			assertThrows(BusinessException.class, () -> authService.signup(param));
		}
	}
}