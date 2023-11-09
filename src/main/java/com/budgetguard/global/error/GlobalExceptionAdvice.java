package com.budgetguard.global.error;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.budgetguard.global.format.ApiResponse;

/**
 * 예외를 전역적으로 처리한다.
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

	/**
	 * 바인딩 예외 처리
	 *
	 * @param e 바인딩 예외
	 * @return 400, 바인딩 예외 메세지
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse> bindException(BindException e) {
		String failMessage = e.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> createFailMessage(
				String.valueOf(fieldError.getRejectedValue()),
				fieldError.getField(),
				fieldError.getDefaultMessage()
			))
			.collect(Collectors.joining(", "));

		return ResponseEntity.badRequest()
			.body(ApiResponse.toFailForm(failMessage));
	}

	/**
	 * 비즈니스 로직 예외 처리
	 *
	 * @param e 비즈니스 로직 예외
	 * @return 비즈니스 로직 예외 메세지
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse> businessException(BusinessException e) {
		String failMessage = createFailMessage(e.getInvalidValue(), e.getFieldName(), e.getMessage());

		return ResponseEntity.status(e.getHttpStatus())
			.body(ApiResponse.toFailForm(failMessage));
	}

	/**
	 * 잘못된 값이 들어왔을 때 메세지 생성
	 *
	 * @param invalidValue 잘못된 값
	 * @param fieldName   필드 이름
	 * @param message    예외 메세지
	 * @return 메세지
	 */
	private String createFailMessage(String invalidValue, String fieldName, String message) {
		return String.format("[%s] 필드에서 잘못된 값 [%s]를 받았습니다. (%s)", fieldName, invalidValue, message);
	}
}
