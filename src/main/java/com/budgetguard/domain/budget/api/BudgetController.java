package com.budgetguard.domain.budget.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.budget.application.BudgetService;
import com.budgetguard.domain.budget.dto.BudgetCreateRequestParam;
import com.budgetguard.global.format.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
public class BudgetController {

	private final BudgetService budgetService;

	/**
	 * 카테고리를 지정하여 예산을 설정합니다.
	 *
	 * @param param 예산 생성 요청 dto
	 * @return 생성된 예산 ID
	 */
	@PostMapping
	public ResponseEntity<ApiResponse> createBudget(@RequestBody BudgetCreateRequestParam param) {
		Long budgetId = budgetService.createBudget(param);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(budgetId));
	}
}
