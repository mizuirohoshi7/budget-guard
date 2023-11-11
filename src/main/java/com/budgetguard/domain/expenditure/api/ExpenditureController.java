package com.budgetguard.domain.expenditure.api;

import static org.springframework.http.HttpHeaders.*;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.expenditure.application.ExpenditureService;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRecommendationResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSearchResponse;
import com.budgetguard.global.config.security.TokenManager;
import com.budgetguard.global.format.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenditures")
public class ExpenditureController {

	private final ExpenditureService expenditureService;
	private final AuthService authService;
	private final TokenManager tokenManager; // 토큰에서 사용자 정보를 가져오기 위해 사용

	/**
	 * 지출을 생성한다.
	 *
	 * @param token JWT 토큰
	 * @param param 지출 생성 요청 파라미터
	 * @return 생성된 지출의 ID
	 */
	@PostMapping
	public ResponseEntity<ApiResponse> createExpenditure(
		@RequestHeader(AUTHORIZATION) String token,
		@RequestBody @Validated ExpenditureCreateRequestParam param
	) {
		// 토큰의 account와 지출을 생성할 account는 같아야 한다.
		authService.validSameTokenAccount(token, param.getMemberId());

		Long expenditureId = expenditureService.createExpenditure(param);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(expenditureId));
	}

	/**
	 * 지출을 수정한다.
	 *
	 * @param token JWT 토큰
	 * @param expenditureId 수정할 지출의 ID
	 * @param param 지출 수정 요청 파라미터
	 * @return 수정된 지출의 ID
	 */
	@PutMapping("/{expenditureId}")
	public ResponseEntity<ApiResponse> updateExpenditure(
		@RequestHeader(AUTHORIZATION) String token,
		@PathVariable Long expenditureId,
		@RequestBody @Validated ExpenditureUpdateRequestParam param
	) {
		// 토큰의 account와 지출을 수정할 account는 같아야 한다.
		authService.validSameTokenAccount(token, param.getMemberId());

		return ResponseEntity.ok(ApiResponse.toSuccessForm(expenditureService.updateExpenditure(expenditureId, param)));
	}

	/**
	 * 지출을 상세 조회한다.
	 *
	 * @param token JWT 토큰
	 * @param expenditureId 조회할 지출의 ID
	 * @return 지출 상세 정보
	 */
	@GetMapping("/{expenditureId}")
	public ResponseEntity<ApiResponse> getExpenditure(
		@RequestHeader(AUTHORIZATION) String token,
		@PathVariable Long expenditureId
	) {
		// 토큰의 account와 지출을 조회할 account는 같아야 한다.
		authService.validSameTokenAccount(token, expenditureService.getExpenditure(expenditureId).getMemberId());

		ExpenditureDetailResponse expenditureDetail = expenditureService.getExpenditure(expenditureId);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(expenditureDetail));
	}

	/**
	 * 지출을 삭제한다.
	 *
	 * @param token JWT 토큰
	 * @param expenditureId 삭제할 지출의 ID
	 * @return 삭제된 지출의 ID
	 */
	@DeleteMapping("/{expenditureId}")
	public ResponseEntity<ApiResponse> deleteExpenditure(
		@RequestHeader(AUTHORIZATION) String token,
		@PathVariable Long expenditureId
	) {
		// 토큰의 account와 지출을 삭제할 account는 같아야 한다.
		authService.validSameTokenAccount(token, expenditureService.getExpenditure(expenditureId).getMemberId());

		return ResponseEntity.ok(ApiResponse.toSuccessForm(expenditureService.deleteExpenditure(expenditureId)));
	}

	/**
	 * 검색 조건으로 지출 목록을 조회한다.
	 *
	 * @param token JWT 토큰
	 * @param param 검색 조건
	 * @return 검색된 지출 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse> searchExpenditures(
		@RequestHeader(AUTHORIZATION) String token,
		@RequestParam Map<String, String> param
	) {
		// 토큰의 account와 지출을 검색할 account는 같아야 한다.
		authService.validSameTokenAccount(token, Long.valueOf(param.get("memberId")));

		// 요청 파라미터를 검색 조건으로 변환한다.
		ExpenditureSearchCond searchCond = toSearchCond(param);

		// 지출 목록을 검색하여 반환한다.
		ExpenditureSearchResponse searchResponse = expenditureService.searchExpenditures(searchCond);
		return ResponseEntity.ok(ApiResponse.toSuccessForm(searchResponse));
	}

	/**
	 * 오늘 지출 금액을 추천받는다.
	 *
	 * @param token JWT 토큰
	 * @return 지출 추천 정보
	 */
	@GetMapping("/recommendation")
	public ResponseEntity<ApiResponse> createExpenditureRecommendation(
		@RequestHeader(AUTHORIZATION) String token
	) {
		// 토큰에서 사용자 계정명을 추출한다.
		String account = tokenManager.getAccountFromToken(token);

		// 지출 추천 정보를 생성한다.
		ExpenditureRecommendationResponse expenditureRecommendation = expenditureService.createExpenditureRecommendation(
			account);

		return ResponseEntity.ok(ApiResponse.toSuccessForm(expenditureRecommendation));
	}

	/**
	 * 요청 파라미터를 검색 조건으로 변환한다.
	 * String을 알맞은 타입으로 변환하는 작업도 수행한다.
	 *
	 * @param param 요청 파라미터
	 * @return 검색 조건
	 */
	private ExpenditureSearchCond toSearchCond(Map<String, String> param) {
		return ExpenditureSearchCond.builder()
			.startDate(toLocalDate(param.get("startDate")))
			.endDate(toLocalDate(param.get("endDate")))
			.categoryName(param.get("categoryName"))
			.minAmount(Integer.valueOf(param.get("minAmount")))
			.maxAmount(Integer.valueOf(param.get("maxAmount")))
			.build();
	}

	/**
	 * String을 LocalDate로 변환한다.
	 *
	 * @param date 변환할 날짜 문자열
	 * @return 변환된 날짜
	 */
	private LocalDate toLocalDate(String date) {
		return LocalDate.parse(date);
	}
}
