package com.budgetguard.domain.member;

import static com.budgetguard.domain.member.entity.MemberRole.*;

import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.domain.member.entity.monthlyoverview.MonthlyOverview;

/**
 * 테스트용 Member 객체를 생성해주는 클래스
 */
public class MemberTestHelper {

	public static Member createMember() {
		return Member.builder()
			.id(1L)
			.monthlyOverview(new MonthlyOverview())
			.account("testAccount")
			.password("Password123!")
			.role(ROLE_USER)
			.build();
	}
}
