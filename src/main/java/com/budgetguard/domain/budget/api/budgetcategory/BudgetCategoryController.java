package com.budgetguard.domain.budget.api.budgetcategory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.global.format.ApiResponse;

@RestController
@RequestMapping("/api/v1/budget-categories")
public class BudgetCategoryController {

	/**
	 * 사용자가 예산 설정에 사용할 수 있도록 모든 예산 카테고리 목록을 조회한다.
	 *
	 * @return 예산 카테고리 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse> getBudgetCategories() {
		List<CategoryName> categoryNames = List.of(CategoryName.values());
		return ResponseEntity.ok(ApiResponse.toSuccessForm(categoryNames));
	}
}
