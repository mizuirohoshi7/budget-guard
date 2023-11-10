package com.budgetguard.domain.expenditure.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
import com.budgetguard.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expenditure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "expenditure_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "budget_category_id")
	private BudgetCategory budgetCategory;

	@Column(nullable = false)
	private Integer amount;

	@Column(nullable = false)
	@CreatedDate
	private LocalDateTime createdTime;

	@Column(nullable = false)
	private String memo;

	// 지출 목록 조회 시, 합계 계산에서 제외 여부 (기본은 제외 안 함)
	@Column(nullable = false)
	@ColumnDefault("false")
	private Boolean isExcluded;

	@Builder
	private Expenditure(Long id, Member member, BudgetCategory budgetCategory, Integer amount, LocalDateTime createdTime,
		String memo, Boolean isExcluded) {
		this.id = id;
		this.member = member;
		this.budgetCategory = budgetCategory;
		this.amount = amount;
		this.createdTime = createdTime;
		this.memo = memo;
		this.isExcluded = isExcluded;
	}

	/**
	 * null 값이 아닌 속성을 수정한다.
	 *
	 * @param expenditure 수정할 지출 내용
	 */
	public void update(Expenditure expenditure) {
		if (expenditure.getAmount() != null) {
			this.amount = expenditure.getAmount();
		}
		if (expenditure.getMemo() != null) {
			this.memo = expenditure.getMemo();
		}
		if (expenditure.getIsExcluded() != null) {
			this.isExcluded = expenditure.getIsExcluded();
		}
	}
}
