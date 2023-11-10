package com.budgetguard.domain.budget;

import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.member.MemberTestHelper;

/**
 * 테스트용 Budget 객체를 생성해주는 클래스
 */
public class BudgetTestHelper {

	public static Budget createBudget() {
		return Budget.builder()
			.id(1L)
			.member(MemberTestHelper.createMember())
			.category(BudgetCategoryTestHelper.createBudgetCategory())
			.amount(100000)
			.build();
	}
}
