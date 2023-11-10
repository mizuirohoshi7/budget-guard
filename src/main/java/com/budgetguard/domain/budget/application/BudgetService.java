package com.budgetguard.domain.budget.application;

import static com.budgetguard.global.error.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.dto.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.dto.BudgetUpdateRequestParam;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
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
		CategoryName categoryName = CategoryName.of(param.getCategoryName());
		BudgetCategory category = budgetCategoryRepository.findByName(categoryName).orElseThrow(
			() -> new BusinessException(param.getCategoryName(), "categoryName", BUDGET_CATEGORY_NOT_FOUND)
		);

		// 예산을 저장한다.
		Budget budget = Budget.builder()
			.member(member)
			.category(category)
			.amount(param.getAmount())
			.build();
		Budget savedBudget = budgetRepository.save(budget);

		// 예산을 사용자의 월간 총 예산에 추가한다.
		changeTotalBudgetAmount(member, param.getAmount());

		return savedBudget.getId();
	}

	/**
	 * 예산을 변경하고 사용자의 월간 오버뷰에 반영합니다.
	 *
	 * @param budgetId 예산 ID
	 * @param param 예산 수정 요청 dto
	 * @return 수정된 예산 ID
	 */
	public Long updateBudget(Long budgetId, BudgetUpdateRequestParam param) {
		Budget budget = budgetRepository.findById(budgetId).orElseThrow(
			() -> new BusinessException(budgetId, "budgetId", BUDGET_NOT_FOUND)
		);

		// 예산 변경량을 측정하기 위해 변경 이전과 이후의 예산을 변수에 저장한다.
		Integer beforeAmount = budget.getAmount();
		budget.updateAmount(param.getAmount());
		Integer afterAmount = budget.getAmount();

		// 예산 변경량을 사용자의 월간 오버뷰에 반영한다.
		Member member = memberRepository.findById(param.getMemberId()).orElseThrow(
			() -> new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
		);
		changeTotalBudgetAmount(member, afterAmount - beforeAmount);

		return budgetId;
	}

	/**
	 * 예산 변경을 사용자의 월간 오버뷰에 반영합니다.
	 *
	 * @param member 사용자
	 * @param amount 예산 변경량 (양수와 음수 모두 가능)
	 */
	private void changeTotalBudgetAmount(Member member, int amount) {
		member.getMonthlyOverview().updateTotalBudgetAmount(amount);
	}
}
