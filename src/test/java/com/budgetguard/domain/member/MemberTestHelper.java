package com.budgetguard.domain.member;

import com.budgetguard.domain.member.entity.Member;

/**
 * 테스트용 Member 객체를 생성해주는 클래스
 */
public class MemberTestHelper {

	public static Member createMember() {
		return Member.builder()
			.id(1L)
			.account("testAccount")
			.password("Password123!")
			.build();
	}
}
