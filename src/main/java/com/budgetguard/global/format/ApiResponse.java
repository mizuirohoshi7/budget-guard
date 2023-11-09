package com.budgetguard.global.format;

import lombok.Getter;

/**
 * JSON 응답 형식
 */
@Getter
public class ApiResponse {

	private static final String STATUS_SUCCESS = "success";
	private static final String STATUS_FAIL = "fail";

	private final String status; // 성공, 실패
	private final String message; // 성공 시 null, 실패 시 실패 메시지
	private final Object data; // 성공 시 데이터, 실패 시 null

	private ApiResponse(String status, String message, Object data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static ApiResponse toSuccessForm(Object data) {
		return new ApiResponse(STATUS_SUCCESS, null, data);
	}

	public static ApiResponse toFailForm(String message) {
		return new ApiResponse(STATUS_FAIL, message, null);
	}
}
