package com.budgetguard.domain.budget.application;

import static com.budgetguard.global.error.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dto.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.budgetcategory.dao.BudgetCategoryRepository;
import com.budgetguard.domain.budgetcategory.entity.BudgetCategory;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BudgetService {

	private final BudgetRepository budgetRepository;
	private final MemberRepository memberRepository;
	private final BudgetCategoryRepository budgetCategoryRepository;

	/**
	 * 카테고리를 지정하여 예산을 설정합니다.
	 *
	 * @param param 예산 생성 요청 dto
	 * @return 생성된 예산 ID
	 */
	public Long createBudget(BudgetCreateRequestParam param) {
		// 요청 dto로부터 사용자를 조회한다.
		Member member = memberRepository.findById(param.getMemberId()).orElseThrow(
			() -> new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
		);

		// 요청 dto로부터 예산 카테고리를 조회한다.
		BudgetCategory category = budgetCategoryRepository.findByName(param.getCategoryName()).orElseThrow(
			() -> new BusinessException(param.getCategoryName(), "categoryName", BUDGET_CATEGORY_NOT_FOUND)
		);

		Budget budget = Budget.builder()
			.member(member)
			.category(category)
			.amount(param.getAmount())
			.build();

		Budget savedBudget = budgetRepository.save(budget);

		return savedBudget.getId();
	}
}
