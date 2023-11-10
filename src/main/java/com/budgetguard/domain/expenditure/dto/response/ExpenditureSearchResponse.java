package com.budgetguard.domain.expenditure.dto.response;

import java.util.List;
import java.util.Map;

import com.budgetguard.domain.budget.constant.CategoryName;

import lombok.Builder;
import lombok.Getter;

/**
 * 지출 검색 응답 dto
 */
@Getter
public class ExpenditureSearchResponse {

	// 지출 목록
	List<ExpenditureSimpleResponse> expenditures;

	// 지출 총 합계
	private final Integer totalAmount;

	// 카테고리별 지출 합계
	private final Map<CategoryName, Integer> amountPerCategory;

	@Builder
	private ExpenditureSearchResponse(List<ExpenditureSimpleResponse> expenditures, Integer totalAmount,
		Map<CategoryName, Integer> amountPerCategory) {
		this.expenditures = expenditures;
		this.totalAmount = totalAmount;
		this.amountPerCategory = amountPerCategory;
	}
}
