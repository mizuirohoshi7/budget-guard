package com.budgetguard.domain.budget.application;

import static com.budgetguard.global.error.ErrorCode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.dto.request.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetRecommendRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetUpdateRequestParam;
import com.budgetguard.domain.budget.dto.response.BudgetRecommendResponse;
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
		int amount = member.getMonthlyOverview().getTotalBudgetAmount() + param.getAmount();
		changeTotalBudgetAmount(member, amount);

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

		// 요청 dto로부터 예산을 조회한다.
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
		int amount = member.getMonthlyOverview().getTotalBudgetAmount() + (afterAmount - beforeAmount);
		changeTotalBudgetAmount(member, amount);

		return budgetId;
	}

	/**
	 * 예산 총액을 바탕으로 예산 추천을 받습니다.
	 * 자동 생성된 예산은, 기존 이용중인 사용자들이 설정한 평균 값 입니다.
	 *
	 * @param param 예산 추천 요청 dto
	 * @return 예산 추천 결과
	 */
	public BudgetRecommendResponse createBudgetRecommendation(BudgetRecommendRequestParam param) {

		// 요청 dto로부터 사용자를 조회한다.
		Member member = memberRepository.findById(param.getMemberId()).orElseThrow(
			() -> new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
		);

		// 기존 이용중인 사용자들이 설정한 예산 비율의 평균을 구한다.
		Map<CategoryName, Double> averageBudgetRates = createAverageBudgetRates();

		// 예산 총액을 카테고리 별 비율로 나눈다.
		Map<CategoryName, Integer> budgetRates = new HashMap<>();
		for (CategoryName categoryName : averageBudgetRates.keySet()) {
			int budgetAmount = (int) (param.getTotalBudgetAmount() * averageBudgetRates.get(categoryName));
			budgetAmount = budgetAmount / 100 * 100; // 100원 단위로 반올림한다.
			budgetRates.put(categoryName, budgetAmount);
		}

		// 예산 총액을 사용자의 월간 오버뷰에 반영한다.
		changeTotalBudgetAmount(member, param.getTotalBudgetAmount());

		return new BudgetRecommendResponse(budgetRates);
	}

	/**
	 * 예산 변경을 사용자의 월간 오버뷰에 반영합니다.
	 *
	 * @param member 사용자
	 * @param amount 변경된 예산
	 */
	private void changeTotalBudgetAmount(Member member, int amount) {
		member.getMonthlyOverview().updateTotalBudgetAmount(amount);
	}

	/**
	 * 기존 이용중인 사용자들이 설정한 예산 비율의 평균을 구한다.
	 *
	 * @return 예산 비율의 평균
	 */
	private Map<CategoryName, Double> createAverageBudgetRates() {

		// 반환할 카테고리 별 예산 비율의 평균
		Map<CategoryName, Double> averageBudgetRates = new HashMap<>();

		// 모든 카테고리를 순회한다.
		for (CategoryName categoryName : CategoryName.values()) {
			// 모든 사용자의 현재 카테고리에 해당하는 예산 비율을 담을 리스트 (후에 사용자 수로 나누어 평균을 내는 데에 쓴다.)
			List<Double> budgetRates = new ArrayList<>();

			// 예산 카테고리의 ID를 구한다.
			BudgetCategory budgetCategory = budgetCategoryRepository.findByName(categoryName).orElseThrow(
				() -> new BusinessException(categoryName.name(), "categoryName", BUDGET_CATEGORY_NOT_FOUND)
			);
			Long budgetCategoryId = budgetCategory.getId();

			// 모든 사용자를 순회한다.
			List<Member> members = memberRepository.findAll();
			for (Member member : members) {
				// 예산 총액
				Integer totalBudgetAmount = member.getMonthlyOverview().getTotalBudgetAmount();

				// 현재 순회 중인 카테고리의 예산 총액
				Integer budgetAmount = budgetRepository.findByMemberIdAndCategoryId(member.getId(), budgetCategoryId)
					.map(Budget::getAmount)
					.orElse(0);

				// 현재 순회 중인 카테고리와 사용자의 예산 비율을 리스트에 더한다.
				Double budgetRate = (double) budgetAmount / totalBudgetAmount;
				budgetRates.add(budgetRate);
			}

			// 모든 사용자의 예산 비율을 리스트에 담았으면 사용자 수로 나누어 평균을 구한다.
			double averageBudgetRate = budgetRates.stream()
				.mapToDouble(Double::doubleValue)
				.average()
				.orElse(0);

			// 평균을 반환할 맵에 추가한다.
			averageBudgetRates.put(categoryName, averageBudgetRate);
		}

		return averageBudgetRates;
	}
}
