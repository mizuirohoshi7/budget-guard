package com.budgetguard.global.util;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.entity.Member;

import lombok.RequiredArgsConstructor;

/**
 * 매 월 1일에 사용자의 예산과 오버뷰를 0원으로 초기화한다.
 */
@Component
@RequiredArgsConstructor
public class BudgetExpenditureResetter {

	private final MemberRepository memberRepository;
	private final BudgetRepository budgetRepository;

	@Scheduled(cron = "0 0 0 1 * *")
	public void resetBudgetExpenditure() {
		List<Member> members = memberRepository.findAll();

		// 모든 사용자의 예산을 0원으로 초기화한다.
		resetBudget(members);

		// 모든 사용자의 오버뷰를 0원으로 초기화한다.
		resetOverview(members);
	}

	/**
	 * 모든 사용자의 예산을 0원으로 초기화한다.
	 *
	 * @param members 사용자 목록
	 */
	private void resetBudget(List<Member> members) {
		for (Member member : members) {
			List<Budget> budgets = budgetRepository.findAllByMemberId(member.getId());
			for (Budget budget : budgets) {
				budget.updateAmount(0);
			}
		}
	}

	/**
	 * 모든 사용자의 오버뷰를 0원으로 초기화한다.
	 *
	 * @param members 사용자 목록
	 */
	private void resetOverview(List<Member> members) {
		for (Member member : members) {
			member.getMonthlyOverview().updateTotalBudgetAmount(0);
			member.getMonthlyOverview().updateTotalExpenditureAmount(0);
		}
	}
}
