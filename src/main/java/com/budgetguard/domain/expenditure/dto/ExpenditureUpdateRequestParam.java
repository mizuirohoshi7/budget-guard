package com.budgetguard.domain.expenditure.dto;

import com.budgetguard.domain.expenditure.entity.Expenditure;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출 수정 요청 파라미터
 */
@Getter
@NoArgsConstructor
public class ExpenditureUpdateRequestParam {

	@NotNull(message = "사용자 ID를 입력해주세요")
	private Long memberId;

	@PositiveOrZero(message = "지출 금액은 0 이상이어야 합니다")
	private Integer amount;

	@NotEmpty(message = "메모를 입력해주세요")
	private String memo;

	@NotNull(message = "합계 제외 여부를 입력해주세요")
	private Boolean isExcluded;

	@Builder
	private ExpenditureUpdateRequestParam(Long memberId, Integer amount, String memo, Boolean isExcluded) {
		this.memberId = memberId;
		this.amount = amount;
		this.memo = memo;
		this.isExcluded = isExcluded;
	}

	/**
	 * 지출 수정 요청 파라미터를 지출 엔티티로 변환한다.
	 * 변환된 엔티티는 지출 엔티티의 수정 메서드에 전달된다.
	 *
	 * @return 수정 내용을 담은 지출 엔티티
	 */
	public Expenditure toEntity() {
		return Expenditure.builder()
			.amount(this.amount)
			.memo(this.memo)
			.isExcluded(this.isExcluded)
			.build();
	}
}
