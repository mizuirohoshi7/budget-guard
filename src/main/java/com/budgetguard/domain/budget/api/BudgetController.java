package com.budgetguard.domain.budget.api;

import static org.springframework.http.HttpHeaders.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.budget.application.BudgetService;
import com.budgetguard.domain.budget.dto.request.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetRecommendRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetUpdateRequestParam;
import com.budgetguard.domain.budget.dto.response.BudgetRecommendResponse;
import com.budgetguard.global.format.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
public class BudgetController {

	private final BudgetService budgetService;
	private final AuthService authService;

	/**
	 * 카테고리를 지정하여 예산을 설정합니다.
	 *
	 * @param param 예산 생성 요청 dto
	 * @return 생성된 예산 ID
	 */
	@PostMapping
	public ResponseEntity<ApiResponse> createBudget(
		@RequestHeader(AUTHORIZATION) String token,
		@RequestBody @Validated BudgetCreateRequestParam param
	) {
		// 토큰의 account와 예산을 생성할 account는 같아야 한다.
		authService.validSameTokenAccount(token, param.getMemberId());

		Long budgetId = budgetService.createBudget(param);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(budgetId));
	}

	/**
	 * 예산을 수정합니다.
	 *
	 * @param token JWT 토큰
	 * @param param 예산 수정 요청 dto
	 * @return 수정된 예산 ID
	 */
	@PutMapping("/{budgetId}")
	public ResponseEntity<ApiResponse> updateBudget(
		@RequestHeader(AUTHORIZATION) String token,
		@PathVariable Long budgetId,
		@RequestBody @Validated BudgetUpdateRequestParam param
	) {
		// 토큰의 account와 예산을 수정할 account는 같아야 한다.
		authService.validSameTokenAccount(token, param.getMemberId());

		return ResponseEntity.ok(ApiResponse.toSuccessForm(budgetService.updateBudget(budgetId, param)));
	}

	/**
	 * 예산 총액을 바탕으로 예산 추천을 받습니다.
	 *
	 * @param token JWT 토큰
	 * @param param 예산 추천 요청 dto
	 * @return 예산 추천 결과
	 */
	@GetMapping("/recommendation")
	public ResponseEntity<ApiResponse> getBudgetRecommendation(
		@RequestHeader(AUTHORIZATION) String token,
		@RequestBody @Validated BudgetRecommendRequestParam param
	) {
		// 토큰의 account와 추천을 받을 account는 같아야 한다.
		authService.validSameTokenAccount(token, param.getMemberId());

		BudgetRecommendResponse budgetRecommendation = budgetService.createBudgetRecommendation(param);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(budgetRecommendation));
	}
}
