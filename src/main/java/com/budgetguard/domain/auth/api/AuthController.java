package com.budgetguard.domain.auth.api;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.auth.dto.request.TokenRequest;
import com.budgetguard.domain.auth.dto.response.TokenResponse;
import com.budgetguard.domain.member.dto.request.MemberLoginRequestParam;
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

	/**
	 * 로그인
	 *
	 * @param param 로그인 입력 데이터
	 * @return 200, 로그인 성공 시 생성된 accessToken, refreshToken을 담은 tokenResponse
	 */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@RequestBody MemberLoginRequestParam param) {
		TokenResponse tokenResponse = authService.login(param);
		return ResponseEntity.ok()
			.body(ApiResponse.toSuccessForm(tokenResponse));
	}

	/**
	 * 토큰 재발급
	 *
	 * @param param accessToken, refreshToken
	 * @return 200, 재발급된 accessToken, refreshToken을 담은 tokenResponse
	 */
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse> reissue(@RequestBody TokenRequest param) {
		TokenResponse tokenResponse = authService.reissue(param);
		return ResponseEntity.ok()
			.body(ApiResponse.toSuccessForm(tokenResponse));
	}
}
