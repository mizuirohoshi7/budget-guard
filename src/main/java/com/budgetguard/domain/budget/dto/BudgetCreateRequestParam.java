package com.budgetguard.domain.budget.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 생성 요청 dto
 */
@Getter
@NoArgsConstructor
public class BudgetCreateRequestParam {

	@NotNull(message = "사용자 ID를 입력해주세요")
	private Long memberId;

	@NotEmpty(message = "카테고리 이름을 입력해주세요")
	private String categoryName;

	@NotNull(message = "예산을 입력해주세요")
	private Integer amount;

	@Builder
	private BudgetCreateRequestParam(Long memberId, String categoryName, Integer amount) {
		this.memberId = memberId;
		this.categoryName = categoryName;
		this.amount = amount;
	}
}
