package com.budgetguard.domain.auth.api;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.global.format.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원 가입
	 *
	 * @param param 회원 가입 입력 데이터
	 * @return 201, 생성된 회원의 ID
	 */
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse> signup(@RequestBody @Validated MemberSignupRequestParam param) {
		param.isValidPassword();

		Long memberId = authService.signup(param);
		return ResponseEntity.status(CREATED)
			.body(ApiResponse.toSuccessForm(memberId));
	}
}
