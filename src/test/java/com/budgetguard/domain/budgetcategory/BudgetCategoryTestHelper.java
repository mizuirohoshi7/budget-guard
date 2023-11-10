package com.budgetguard.domain.budgetcategory;

import static com.budgetguard.domain.budgetcategory.constant.CategoryName.*;

import com.budgetguard.domain.budgetcategory.entity.BudgetCategory;

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
