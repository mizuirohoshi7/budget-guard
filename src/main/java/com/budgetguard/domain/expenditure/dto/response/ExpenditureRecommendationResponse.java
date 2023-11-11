package com.budgetguard.domain.expenditure.dto.response;

import java.util.Map;

import com.budgetguard.domain.budget.constant.CategoryName;

import lombok.Builder;
import lombok.Getter;

/**
 * 지출 추천 응답 DTO
 */
@Getter
public class ExpenditureRecommendationResponse {

	// 오늘 지출 가능한 총 금액
	private final Integer totalAmount;

	// 오늘 카테고리별 지출 가능한 금액
	private final Map<CategoryName, Integer> amountPerCategory;

	// 사용자의 지출 상황에 따른 메세지
	private final String expenditureMessage;

	@Builder
	private ExpenditureRecommendationResponse(Integer totalAmount, Map<CategoryName, Integer> amountPerCategory,
		String expenditureMessage) {
		this.totalAmount = totalAmount;
		this.amountPerCategory = amountPerCategory;
		this.expenditureMessage = expenditureMessage;
	}
}
