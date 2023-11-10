package com.budgetguard.domain.expenditure.api;

import static org.springframework.http.HttpHeaders.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.expenditure.application.ExpenditureService;
import com.budgetguard.domain.expenditure.dto.ExpenditureCreateRequestParam;
import com.budgetguard.global.format.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenditures")
public class ExpenditureController {

	private final ExpenditureService expenditureService;
	private final AuthService authService;

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
}
