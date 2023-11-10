package com.budgetguard.domain.expenditure.application;

import static com.budgetguard.global.error.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
import com.budgetguard.domain.expenditure.dao.ExpenditureRepository;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSearchResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSimpleResponse;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenditureService {

	private final ExpenditureRepository expenditureRepository;
	private final MemberRepository memberRepository;
	private final BudgetCategoryRepository budgetCategoryRepository;

	/**
	 * 지출을 생성한다.
	 *
	 * @param param 지출 생성 요청 파라미터
	 * @return 생성된 지출의 ID
	 */
	public Long createExpenditure(ExpenditureCreateRequestParam param) {

		// 지출 생성 요청 파라미터의 memberId로 회원을 찾는다.
		Member member = memberRepository.findById(param.getMemberId()).orElseThrow(
			() -> new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
		);

		// 지출 생성 요청 파라미터의 categoryName으로 예산 카테고리를 찾는다.
		CategoryName categoryName = CategoryName.of(param.getCategoryName());
		BudgetCategory budgetCategory = budgetCategoryRepository.findByName(categoryName).orElseThrow(
			() -> new BusinessException(param.getCategoryName(), "categoryName", BUDGET_CATEGORY_NOT_FOUND)
		);

		// 지출을 저장한다.
		Expenditure expenditure = Expenditure.builder()
			.member(member)
			.budgetCategory(budgetCategory)
			.amount(param.getAmount())
			.memo(param.getMemo())
			.isExcluded(param.getIsExcluded())
			.build();
		Expenditure savedExpenditure = expenditureRepository.save(expenditure);

		// 사용자의 월간 오버뷰에 반영한다.
		int amount = member.getMonthlyOverview().getTotalExpenditureAmount() + param.getAmount();
		changeTotalExpenditureAmount(member, amount);

		return savedExpenditure.getId();
	}

	/**
	 * 지출을 수정한다.
	 *
	 * @param expenditureId 수정할 지출의 ID
	 * @param param 지출 수정 요청 파라미터
	 * @return 수정된 지출의 ID
	 */
	public Long updateExpenditure(Long expenditureId, ExpenditureUpdateRequestParam param) {

		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(
			() -> new BusinessException(expenditureId, "expenditureId", ErrorCode.EXPENDITURE_NOT_FOUND)
		);

		// 지출을 수정한다.
		int beforeAmount = expenditure.getAmount();
		expenditure.update(param.toEntity());
		int afterAmount = expenditure.getAmount();

		// 수정된 지출을 사용자의 월간 오버뷰에 반영한다.
		Member member = memberRepository.findById(param.getMemberId()).orElseThrow(
			() -> new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
		);
		int amount = member.getMonthlyOverview().getTotalExpenditureAmount() + (afterAmount - beforeAmount);
		changeTotalExpenditureAmount(member, amount);

		return expenditure.getId();
	}

	/**
	 * 지출을 상세 조회한다.
	 *
	 * @param expenditureId 조회할 지출의 ID
	 * @return 지출 상세 조회 응답
	 */
	public ExpenditureDetailResponse getExpenditure(Long expenditureId) {
		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(
			() -> new BusinessException(expenditureId, "expenditureId", ErrorCode.EXPENDITURE_NOT_FOUND)
		);

		return new ExpenditureDetailResponse(expenditure);
	}

	/**
	 * 지출을 삭제한다.
	 *
	 * @param expenditureId 삭제할 지출의 ID
	 * @return 삭제된 지출의 ID
	 */
	public Long deleteExpenditure(Long expenditureId) {

		Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(
			() -> new BusinessException(expenditureId, "expenditureId", ErrorCode.EXPENDITURE_NOT_FOUND)
		);

		// 삭제된 지출을 사용자의 월간 오버뷰에 반영한다.
		Member member = expenditure.getMember();
		int amount = member.getMonthlyOverview().getTotalExpenditureAmount() - expenditure.getAmount();
		changeTotalExpenditureAmount(member, amount);

		expenditureRepository.delete(expenditure);

		return expenditureId;
	}

	/**
	 * 검색 조건으로 지출을 조회한다.
	 *
	 * @param searchCond 검색 조건
	 * @return 검색된 지출 목록
	 */
	public ExpenditureSearchResponse searchExpenditures(ExpenditureSearchCond searchCond) {

		// 검색 조건에 맞는 Expenditure 엔티티 리스트를 가져온다.
		List<Expenditure> expenditures = expenditureRepository.searchExpenditures(searchCond);

		// 지출 목록을 응답 dto로 변환한다.
		List<ExpenditureSimpleResponse> expenditureSimpleResponses = expenditures.stream()
			.map(ExpenditureSimpleResponse::new)
			.collect(Collectors.toList());

		// 지출 총 합계를 계산한다.
		Integer totalAmount = calculateTotalAmount(expenditures);

		// 카테고리별 지출 합계를 계산한다.
		Map<CategoryName, Integer> amountPerCategory = calculateAmountPerCategory(expenditures);

		return ExpenditureSearchResponse.builder()
			.expenditures(expenditureSimpleResponses)
			.totalAmount(totalAmount)
			.amountPerCategory(amountPerCategory)
			.build();
	}

	/**
	 * 지출 변경을 사용자의 월간 오버뷰에 반영합니다.
	 *
	 * @param member 사용자
	 * @param amount 지출 변경량
	 */
	private void changeTotalExpenditureAmount(Member member, int amount) {
		member.getMonthlyOverview().updateTotalExpenditureAmount(amount);
	}

	/**
	 * 지출 총 합계를 계산한다.
	 *
	 * @param expenditures 지출 목록
	 * @return 지출 총 합계
	 */
	private Integer calculateTotalAmount(List<Expenditure> expenditures) {
		return expenditures.stream()
			.mapToInt(Expenditure::getAmount)
			.sum();
	}

	/**
	 * 카테고리별 지출 합계를 계산한다.
	 *
	 * @param expenditures 지출 목록
	 * @return 카테고리별 지출 합계
	 */
	private Map<CategoryName, Integer> calculateAmountPerCategory(List<Expenditure> expenditures) {
		return expenditures.stream()
			.collect(Collectors.groupingBy(
				expenditure -> expenditure.getBudgetCategory().getName(),
				Collectors.summingInt(Expenditure::getAmount)
			));
	}
}
