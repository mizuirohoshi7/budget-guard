package com.budgetguard.domain.expenditure.dto.response;

import com.budgetguard.domain.expenditure.entity.Expenditure;

import lombok.Getter;

/**
 * 지출 검색 목록에서 보이는 간단한 응답 dto
 */
@Getter
public class ExpenditureSimpleResponse {

	private final Long memberId;
	private final Long budgetCategoryId;
	private final Integer amount;

	public ExpenditureSimpleResponse(Expenditure expenditure) {
		this.memberId = expenditure.getMember().getId();
		this.budgetCategoryId = expenditure.getBudgetCategory().getId();
		this.amount = expenditure.getAmount();
	}
}
