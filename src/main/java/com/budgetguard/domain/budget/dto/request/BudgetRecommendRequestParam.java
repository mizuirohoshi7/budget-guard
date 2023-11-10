package com.budgetguard.domain.budget.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 추천 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BudgetRecommendRequestParam {

	@NotNull(message = "사용자 ID를 입력해주세요")
	private Long memberId;

	@PositiveOrZero(message = "예산은 0 이상이어야 합니다")
	private Integer totalBudgetAmount;

	@Builder
	private BudgetRecommendRequestParam(Long memberId, Integer totalBudgetAmount) {
		this.memberId = memberId;
		this.totalBudgetAmount = totalBudgetAmount;
	}
}
