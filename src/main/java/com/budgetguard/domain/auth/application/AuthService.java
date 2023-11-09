package com.budgetguard.domain.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원 가입
	 *
	 * @param param 회원 가입 입력 데이터
	 * @return 생성된 회원의 id
	 */
	public Long signup(MemberSignupRequestParam param) {
		checkDuplicatedAccount(param.getAccount());

		Member member = param.toEntity(passwordEncoder);
		Member savedMember = memberRepository.save(member);
		return savedMember.getId();
	}

	/**
	 * 회원 아이디 중복 확인
	 * 중복이면 예외 처리
	 *
	 * @param account 확인할 회원 아이디
	 */
	private void checkDuplicatedAccount(String account) {
		if (memberRepository.findByAccount(account).isPresent()) {
			throw new BusinessException(account, "account", ErrorCode.DUPLICATED_ACCOUNT);
		}
	}
}
