package com.budgetguard.domain.expenditure.dto.response;

import java.time.LocalDateTime;

import com.budgetguard.domain.expenditure.entity.Expenditure;

import lombok.Getter;

/**
 * 지출 상세 조회 응답 dto
 */
@Getter
public class ExpenditureDetailResponse {

	private final Long memberId;
	private final Long budgetCategoryId;
	private final Integer amount;
	private final String memo;
	private final Boolean isExcluded;
	private final LocalDateTime createdTime;

	public ExpenditureDetailResponse(Expenditure expenditure) {
		this.memberId = expenditure.getMember().getId();
		this.budgetCategoryId = expenditure.getBudgetCategory().getId();
		this.amount = expenditure.getAmount();
		this.memo = expenditure.getMemo();
		this.isExcluded = expenditure.getIsExcluded();
		this.createdTime = expenditure.getCreatedTime();
	}
}
