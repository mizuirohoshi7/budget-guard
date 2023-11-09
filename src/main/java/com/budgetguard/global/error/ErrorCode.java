package com.budgetguard.global.error;

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
	INVALID_PASSWORD_CONFIRM("비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	DUPLICATED_ACCOUNT("이미 사용중인 계정명입니다.", HttpStatus.BAD_REQUEST),

	// 인가 인증
	ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	UNAUTHORIZED_ENTRY_POINT("유효하지 않은 자격 증명입니다.", HttpStatus.UNAUTHORIZED);

	private final String message;
	private final HttpStatus httpStatus;
}
