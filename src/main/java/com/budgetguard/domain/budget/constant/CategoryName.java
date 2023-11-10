package com.budgetguard.domain.budget.constant;

import static com.budgetguard.global.error.ErrorCode.*;

import com.budgetguard.global.error.BusinessException;

/**
 * 예산 카테고리의 종류
 */
public enum CategoryName {

	FOOD, // 식비
	TRANSPORTATION, // 교통비
	ENTERTAINMENT // 여가비
	;

	/**
	 * 예산 카테고리의 이름을 반환한다.
	 * 예외 발생 시, 커스텀한 예외를 발생하기 위해 만든 메서드
	 *
	 * @param name 예산 카테고리의 이름
	 * @return 예산 카테고리
	 */
	public static CategoryName of(String name) {
		try {
			return CategoryName.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new BusinessException(name, "categoryName", BUDGET_CATEGORY_NOT_FOUND);
		}
	}
}
