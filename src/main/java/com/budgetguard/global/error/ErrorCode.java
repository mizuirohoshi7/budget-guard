package com.budgetguard.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 로직 예외 메세지 및 상태 코드
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 회원가입
	INVALID_PASSWORD_CONFIRM("비밀번호와 비밀번호 확인이 일치하지 않습니다.", BAD_REQUEST),
	DUPLICATED_ACCOUNT("이미 사용중인 계정명입니다.", BAD_REQUEST),

	// 로그인
	WRONG_PASSWORD("비밀번호가 일치하지 않습니다.", BAD_REQUEST),

	// 토큰 재발급
	INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", BAD_REQUEST),
	MEMBER_LOGOUT("로그아웃된 회원입니다.", BAD_REQUEST),
	REFRESH_TOKEN_MISMATCH("리프레시 토큰이 일치하지 않습니다.", BAD_REQUEST),

	// 인가 인증
	ACCESS_DENIED("접근 권한이 없습니다.", FORBIDDEN),
	UNAUTHORIZED_ENTRY_POINT("유효하지 않은 자격 증명입니다.", UNAUTHORIZED),
	MEMBER_ACCOUNT_NOT_FOUND("존재하지 않는 계정입니다.", BAD_REQUEST)
	;

	private final String message;
	private final HttpStatus httpStatus;
}
