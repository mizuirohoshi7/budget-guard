package com.budgetguard.domain.budget;

import static com.budgetguard.domain.budget.constant.CategoryName.*;

import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;

/**
 * 테스트용 BudgetCategory 객체를 생성해주는 클래스
 */
public class BudgetCategoryTestHelper {

	public static BudgetCategory createBudgetCategory() {
		return BudgetCategory.builder()
			.id(1L)
			.name(FOOD)
			.build();
	}
}
