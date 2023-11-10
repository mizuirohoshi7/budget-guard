package com.budgetguard.domain.budget.dto.response;

import java.util.Map;

import com.budgetguard.domain.budget.constant.CategoryName;

import lombok.Getter;

@Getter
public class BudgetRecommendResponse {

	// 카테고리 별 예산 추천 금액
	Map<CategoryName, Integer> budgetRecommendAmountPerCategory;

	public BudgetRecommendResponse(Map<CategoryName, Integer> budgetRecommendAmountPerCategory) {
		this.budgetRecommendAmountPerCategory = budgetRecommendAmountPerCategory;
	}
}
