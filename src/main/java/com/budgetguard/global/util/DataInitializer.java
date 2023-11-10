package com.budgetguard.global.util;

import static com.budgetguard.domain.member.entity.MemberRole.*;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.domain.member.entity.monthlyoverview.MonthlyOverview;

import lombok.RequiredArgsConstructor;

/**
 * 더미 데이터 초기화용 클래스
 */
@Component
@Transactional
@RequiredArgsConstructor
public class DataInitializer {

	private final BudgetCategoryRepository budgetCategoryRepository;
	private final MemberRepository memberRepository;
	private final BudgetRepository budgetRepository;

	/**
	 * 애플리케이션이 실행되면 더미 데이터를 초기화한다.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initData() {
		initBudgetCategory();
		initMemberAndOverView();
		initBudget();
	}

	/**
	 * 예산 카테고리 데이터를 초기화한다.
	 */
	private void initBudgetCategory() {
		for (CategoryName name : CategoryName.values()) {
			budgetCategoryRepository.save(BudgetCategory.builder().name(name).build());
		}
	}

	/**
	 * 사용자 데이터와 오버뷰 데이터를 초기화한다.
	 * cascade = ALL과 orphanRemoval = true로 설정해두었기 때문에 오버뷰 데이터는 자동으로 저장된다.
	 */
	private void initMemberAndOverView() {
		for (int i = 1; i <= 10; i++) {
			Member member = Member.builder()
				.monthlyOverview(new MonthlyOverview())
				.role(ROLE_USER)
				.account("account" + i)
				.password("password" + i)
				.build();
			memberRepository.save(member);
		}
	}

	/**
	 * 임의의 카테고리, 임의의 금액을 가진 예산 데이터를 초기화한다.
	 * 추가한 예산만큼 오버뷰 데이터도 갱신한다.
	 */
	private void initBudget() {
		CategoryName[] categories = CategoryName.values();

		for (int i = 1; i <= 10; i++) {
			Member member = memberRepository.findById((long) i).orElseThrow();
			int budgetAmount = createRandomBudgetAmount();

			// 예산 데이터 저장
			Budget budget = Budget.builder()
				// 존재하는 카테고리 중 랜덤한 카테고리를 선택한다.
				.category(budgetCategoryRepository.findByName(categories[(int) (Math.random() * 3)]).orElseThrow())
				.member(member)
				.amount(budgetAmount)
				.build();
			budgetRepository.save(budget);

			// 오버뷰 데이터 갱신
			member.getMonthlyOverview().updateTotalBudgetAmount(budgetAmount);
		}
	}

	/**
	 * 100원 이상 10100원 이하의 임의의 금액을 생성한다.
	 *
	 * @return 생성된 금액
	 */
	private int createRandomBudgetAmount() {
		return (int) ((Math.random() * 100) + 1) * 100;
	}
}
