package com.budgetguard.domain.expenditure.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출 목록 검색 조건
 */
@Getter
@NoArgsConstructor
public class ExpenditureSearchCond {

	@NotNull(message = "사용자 ID를 입력해주세요")
	private Long memberId;

	@NotNull(message = "조회 시작일을 입력해주세요")
	private LocalDate startDate;

	@NotNull(message = "조회 종료일을 입력해주세요")
	private LocalDate endDate;

	// null이면 모든 카테고리를 조회한다.
	private String categoryName;

	// null이면 0원부터 조회한다.
	private Integer minAmount;

	// null이면 Integer.MAX_VALUE원까지 조회한다.
	private Integer maxAmount;

	@Builder
	private ExpenditureSearchCond(Long memberId, LocalDate startDate, LocalDate endDate, String categoryName,
		Integer minAmount, Integer maxAmount) {
		this.memberId = memberId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.categoryName = categoryName;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
}
