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

	EXAMPLE_ERROR("예시 에러", HttpStatus.BAD_REQUEST);

	private final String message;
	private final HttpStatus httpStatus;
}
