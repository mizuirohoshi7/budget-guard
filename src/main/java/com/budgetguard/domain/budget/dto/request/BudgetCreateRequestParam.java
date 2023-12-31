package com.budgetguard.domain.budget.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
	@PositiveOrZero(message = "예산은 0 이상이어야 합니다")
	private Integer amount;

	@Builder
	private BudgetCreateRequestParam(Long memberId, String categoryName, Integer amount) {
		this.memberId = memberId;
		this.categoryName = categoryName;
		this.amount = amount;
	}
}
