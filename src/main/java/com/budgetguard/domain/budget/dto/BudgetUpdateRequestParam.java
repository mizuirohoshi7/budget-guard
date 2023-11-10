package com.budgetguard.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BudgetUpdateRequestParam {

	@NotNull(message = "사용자 ID를 입력해주세요")
	private Long memberId;

	@NotNull(message = "수정할 예산을 입력해주세요")
	@PositiveOrZero(message = "수정할 예산은 0 이상이어야 합니다")
	private Integer amount;

	@Builder
	private BudgetUpdateRequestParam(Long memberId, Integer amount) {
		this.memberId = memberId;
		this.amount = amount;
	}
}
