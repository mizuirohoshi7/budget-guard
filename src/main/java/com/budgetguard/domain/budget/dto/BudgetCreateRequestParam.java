package com.budgetguard.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 생성 요청 dto
 */
@Getter
@NoArgsConstructor
public class BudgetCreateRequestParam {

	private Long memberId;
	private String categoryName;
	private Integer amount;

	@Builder
	private BudgetCreateRequestParam(Long memberId, String categoryName, Integer amount) {
		this.memberId = memberId;
		this.categoryName = categoryName;
		this.amount = amount;
	}
}
