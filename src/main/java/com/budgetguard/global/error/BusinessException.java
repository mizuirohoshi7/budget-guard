package com.budgetguard.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 */
@Getter
public class BusinessException extends RuntimeException {

	private final String invalidValue; // 예외 발생 값
	private final String fieldName; // 예외 발생 필드명
	private final String message; // 예외 메세지
	private final HttpStatus httpStatus; // 예외 상태 코드

	public BusinessException(Object invalidValue, String fieldName, ErrorCode errorCode) {
		this.invalidValue = invalidValue != null ? invalidValue.toString() : null;
		this.fieldName = fieldName;
		this.message = errorCode.getMessage();
		this.httpStatus = errorCode.getHttpStatus();
	}
}
