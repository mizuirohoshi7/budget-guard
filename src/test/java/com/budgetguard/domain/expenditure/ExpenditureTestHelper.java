package com.budgetguard.domain.expenditure;

import static java.time.LocalDateTime.*;

import com.budgetguard.domain.budget.BudgetCategoryTestHelper;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.budgetguard.domain.member.MemberTestHelper;

/**
 * 테스트용 Expenditure 객체를 생성해주는 클래스
 */
public class ExpenditureTestHelper {

	public static Expenditure createExpenditure() {
		return Expenditure.builder()
			.id(1L)
			.member(MemberTestHelper.createMember())
			.budgetCategory(BudgetCategoryTestHelper.createBudgetCategory())
			.amount(10000)
			.createdTime(now())
			.memo("메모입니다.")
			.isExcluded(false)
			.build();
	}
}
