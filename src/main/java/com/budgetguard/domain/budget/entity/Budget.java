package com.budgetguard.domain.budget.entity;

import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
import com.budgetguard.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "budget_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "budget_category_id")
	private BudgetCategory category;

	@Column(nullable = false)
	private Integer amount;

	@Builder
	private Budget(Long id, Member member, BudgetCategory category, Integer amount) {
		this.id = id;
		this.member = member;
		this.category = category;
		this.amount = amount;
	}
}
