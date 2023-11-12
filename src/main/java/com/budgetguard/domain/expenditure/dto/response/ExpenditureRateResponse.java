package com.budgetguard.domain.expenditure.dto.response;

import java.util.Map;

import com.budgetguard.domain.budget.constant.CategoryName;

import lombok.Builder;
import lombok.Getter;

/**
 * 지출 소비율 응답
 */
@Getter
public class ExpenditureRateResponse {

	// (지난 달, 지난 요일, 다른 사용자) 대비 총 소비율
	private Integer totalRate;

	// (지난 달, 지난 요일, 다른 사용자) 대비 카테고리 별 소비율
	private Map<CategoryName, Integer> ratePerCategory;

	@Builder
	private ExpenditureRateResponse(Integer totalRate, Map<CategoryName, Integer> ratePerCategory) {
		this.totalRate = totalRate;
		this.ratePerCategory = ratePerCategory;
	}
}
