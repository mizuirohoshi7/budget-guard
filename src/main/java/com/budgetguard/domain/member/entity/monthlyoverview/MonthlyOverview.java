package com.budgetguard.domain.member.entity.monthlyoverview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 월별 예산 총액, 지출 총액을 저장하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
public class MonthlyOverview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "monthly_overview_id")
	private Long id;

	@Column(nullable = false)
	private Integer totalBudgetAmount = 0; // 예산 총액

	@Column(nullable = false)
	private Integer totalExpenditureAmount = 0; // 지출 총액

	// 예산을 추가하거나 삭제할 때마다 예산 총액을 변경해주는 메서드
	public void updateTotalBudgetAmount(int changedAmount) {
		this.totalBudgetAmount += changedAmount;
	}

	// 지출을 추가하거나 삭제할 때마다 지출 총액을 변경해주는 메서드
	public void updateTotalExpenditureAmount(int changedAmount) {
		this.totalExpenditureAmount += changedAmount;
	}
}
