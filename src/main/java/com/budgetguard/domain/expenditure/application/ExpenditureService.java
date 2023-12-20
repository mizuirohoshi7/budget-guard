package com.budgetguard.domain.expenditure.application;

import static com.budgetguard.domain.expenditure.constant.ExpenditureMessage.*;
import static com.budgetguard.global.error.ErrorCode.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;
import com.budgetguard.domain.expenditure.dao.ExpenditureRepository;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRateResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRecommendationResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSearchResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSimpleResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureTodayResponse;
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

	// 예산을 초과했더라도 추천받는 최소 금액
	private static final int MINIMUM_EXPENDITURE_AMOUNT = 5000;

	private final ExpenditureRepository expenditureRepository;
	private final MemberRepository memberRepository;
	private final BudgetCategoryRepository budgetCategoryRepository;
	private final BudgetRepository budgetRepository;

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
			.filter(expenditure -> !expenditure.getIsExcluded())
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
	 * 오늘 지출 추천을 반환한다.
	 *
	 * @param account 사용자 계정명
	 * @return 지출 추천
	 */
	public ExpenditureRecommendationResponse createExpenditureRecommendation(String account) {

		// 요청한 사용자를 조회한다.
		Member member = memberRepository.findByAccount(account).orElseThrow(
			() -> new BusinessException(account, "account", MEMBER_NOT_FOUND)
		);

		// 사용자의 카테고리별 예산 비율을 계산한다.
		Map<CategoryName, Double> categoryRates = calculateCategoryRates(member);

		// 현재 남아있는 예산을 이번 달의 남은 일수로 나누어 오늘 지출 가능한 총액을 구한다.
		Integer todayBudget = calculateTodayBudget(member);

		// 오늘 지출 가능한 총액을 예산 비율만큼 나누어 지출 추천으로 만든다.
		Map<CategoryName, Integer> amountPerCategory = calculateAmountPerCategoryByRates(categoryRates, todayBudget);

		// 지출 추천 중 최소 금액보다 적은 지출은 최소 금액으로 설정한다.
		amountPerCategory.entrySet().stream()
			.filter(entry -> entry.getValue() < MINIMUM_EXPENDITURE_AMOUNT)
			.forEach(entry -> entry.setValue(MINIMUM_EXPENDITURE_AMOUNT));

		// 최소 금액으로 변경될 여지가 있으므로 오늘 지출 가능한 총액을 갱신한다.
		int updatedTodayBudget = amountPerCategory.values().stream()
			.mapToInt(Integer::intValue)
			.sum();

		// 지출 추천 메시지를 생성한다.
		String expenditureMessage = createExpenditureMessage(member);

		return ExpenditureRecommendationResponse.builder()
			.totalAmount(updatedTodayBudget)
			.amountPerCategory(amountPerCategory)
			.expenditureMessage(expenditureMessage)
			.build();
	}

	/**
	 * 오늘 지출 정보를 생성한다.
	 *
	 * @param account 사용자 계정명
	 * @return 오늘 지출 정보
	 */
	public ExpenditureTodayResponse createExpenditureToday(String account) {

		// 요청한 사용자를 조회한다.
		Member member = memberRepository.findByAccount(account).orElseThrow(
			() -> new BusinessException(account, "account", MEMBER_NOT_FOUND)
		);

		// 사용자의 지출 목록을 조회한다.
		List<Expenditure> expenditures = expenditureRepository.findAllByMemberId(member.getId());

		// 지출 목록 중 일자가 오늘이 아닌 지출은 제외한다.
		expenditures = expenditures.stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().equals(LocalDate.now()))
			.collect(Collectors.toList());

		// 지출 총 합계를 계산한다.
		Integer totalAmount = calculateTotalAmount(expenditures);

		// 카테고리별 지출 합계를 계산한다.
		Map<CategoryName, Integer> amountPerCategory = calculateAmountPerCategory(expenditures);

		// 사용자의 카테고리별 예산 비율을 계산한다.
		Map<CategoryName, Double> categoryRates = calculateCategoryRates(member);

		// 현재 남아있는 예산을 이번 달의 남은 일수로 나누어 적절 지출 가능한 총액을 구한다.
		Integer properTotalAmount = calculateTodayBudget(member);

		// 적절 지출 총액을 예산 비율만큼 나누어 카테고리 별 적절 지출 금액을 만든다.
		Map<CategoryName, Integer> properAmountPerCategory = calculateAmountPerCategoryByRates(categoryRates,
			properTotalAmount);

		// 위험도를 계산한다.
		Map<CategoryName, Integer> dangerRates = calculateDangerRates(amountPerCategory, properAmountPerCategory);

		return ExpenditureTodayResponse.builder()
			.totalAmount(totalAmount)
			.amountPerCategory(amountPerCategory)
			.properTotalAmount(properTotalAmount)
			.properAmountPerCategory(properAmountPerCategory)
			.dangerRates(dangerRates)
			.build();
	}

	/**
	 * 지난 달 대비 총액, 카테고리 별 소비율을 생성한다.
	 *
	 * @param account 사용자 계정명
	 * @return 지난 달 대비 소비율
	 */
	public ExpenditureRateResponse createExpenditureMonthlyRate(String account) {

		// 요청한 사용자를 조회한다.
		Member member = memberRepository.findByAccount(account).orElseThrow(
			() -> new BusinessException(account, "account", MEMBER_NOT_FOUND)
		);

		// 사용자의 지출 목록을 조회한다.
		List<Expenditure> expenditures = expenditureRepository.findAllByMemberId(member.getId());

		// 지난 달의 오늘까지의 지출 목록을 조회한다.
		List<Expenditure> expendituresOfLastMonth = expenditures.stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().getMonthValue() ==
				LocalDate.now().minusMonths(1).getMonthValue())
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().getDayOfMonth() <=
				LocalDate.now().getDayOfMonth())
			.toList();

		// 이번 달의 지출 목록을 조회한다.
		List<Expenditure> expendituresOfThisMonth = expenditures.stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().getMonthValue() ==
				LocalDate.now().getMonthValue())
			.toList();

		// 지난 달 대비 이번 달 지출 총 액의 비율을 구한다.
		int totalRate = (int) ((double) calculateTotalAmount(expendituresOfThisMonth) /
			calculateTotalAmount(expendituresOfLastMonth) * 100);

		// 지난 달 대비 이번 달 카테고리별 지출 총 액의 비율을 구한다.
		Map<CategoryName, Integer> lastMonthExpenditureAmounts = calculateAmountPerCategory(expendituresOfThisMonth);
		Map<CategoryName, Integer> thisMonthExpenditureAmounts = calculateAmountPerCategory(expendituresOfThisMonth);
		Map<CategoryName, Integer> ratePerCategory = lastMonthExpenditureAmounts.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> (int) ((double) thisMonthExpenditureAmounts.get(entry.getKey()) /
					entry.getValue() * 100)
			));

		return ExpenditureRateResponse.builder()
			.totalRate(totalRate)
			.ratePerCategory(ratePerCategory)
			.build();
	}

	/**
	 * 지난 요일 대비 총액, 카테고리 별 소비율을 생성한다.
	 *
	 * @param account 사용자 계정명
	 * @return 지난 요일 대비 소비율
	 */
	public ExpenditureRateResponse createExpenditureDayOfWeekRate(String account) {

		// 요청한 사용자를 조회한다.
		Member member = memberRepository.findByAccount(account).orElseThrow(
			() -> new BusinessException(account, "account", MEMBER_NOT_FOUND)
		);

		// 사용자의 지출 목록을 조회한다.
		List<Expenditure> expenditures = expenditureRepository.findAllByMemberId(member.getId());

		// 오늘과 같은 요일의 지난 지출 목록을 조회한다.
		List<Expenditure> expendituresOfLastDayOfWeek = expenditures.stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().getDayOfWeek() ==
				LocalDate.now().getDayOfWeek())
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().isBefore(LocalDate.now()))
			.toList();

		// 오늘의 지출 목록을 조회한다.
		List<Expenditure> expendituresOfToday = expenditures.stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().equals(LocalDate.now()))
			.toList();

		// 지난 요일 대비 이번달 지출 총 액의 비율을 구한다.
		int totalRate = (int) ((double) calculateTotalAmount(expendituresOfToday) /
			calculateTotalAmount(expendituresOfLastDayOfWeek) / expendituresOfLastDayOfWeek.size() * 100);

		// 지난 요일 대비 이번 달 카테고리별 지출 총 액의 비율을 구한다.
		Map<CategoryName, Integer> lastDayOfWeekExpenditureAmounts = calculateAmountPerCategory(expendituresOfLastDayOfWeek);
		Map<CategoryName, Integer> thisDayOfWeekExpenditureAmounts = calculateAmountPerCategory(expendituresOfToday);
		Map<CategoryName, Integer> ratePerCategory = lastDayOfWeekExpenditureAmounts.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> (int) ((double) thisDayOfWeekExpenditureAmounts.get(entry.getKey()) /
					entry.getValue() / expendituresOfLastDayOfWeek.size() * 100)
			));

		return ExpenditureRateResponse.builder()
			.totalRate(totalRate)
			.ratePerCategory(ratePerCategory)
			.build();
	}

	/**
	 * 다른 사용자 대비 총액, 카테고리 별 소비율을 생성한다.
	 *
	 * @param account 사용자 계정명
	 * @return 다른 사용자 대비 소비율
	 */
	public ExpenditureRateResponse createOtherMemberExpenditureRate(String account) {

		// 요청한 사용자를 조회한다.
		Member member = memberRepository.findByAccount(account).orElseThrow(
			() -> new BusinessException(account, "account", MEMBER_NOT_FOUND)
		);

		// 사용자의 오늘 지출 목록을 조회한다.
		List<Expenditure> expenditures = expenditureRepository.findAllByMemberId(member.getId()).stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().equals(LocalDate.now()))
			.toList();;

		// 다른 사용자의 오늘 지출 목록을 조회한다.
		List<Expenditure> otherExpenditures = expenditureRepository.findAll().stream()
			.filter(expenditure -> expenditure.getCreatedTime().toLocalDate().equals(LocalDate.now()))
			.toList();

		// 다른 사용자 대비 이번 달 지출 총 액의 비율을 구한다.
		int totalRate = (int) ((double) calculateTotalAmount(expenditures) /
			calculateTotalAmount(otherExpenditures) / otherExpenditures.size() * 100);

		// 다른 사용자 대비 이번 달 카테고리별 지출 총 액의 비율을 구한다.
		Map<CategoryName, Integer> otherMemberExpenditureAmounts = calculateAmountPerCategory(otherExpenditures);
		Map<CategoryName, Integer> thisMemberExpenditureAmounts = calculateAmountPerCategory(expenditures);
		Map<CategoryName, Integer> ratePerCategory = otherMemberExpenditureAmounts.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> (int) ((double) thisMemberExpenditureAmounts.get(entry.getKey()) /
					entry.getValue() / otherExpenditures.size() * 100)
			));

		return ExpenditureRateResponse.builder()
			.totalRate(totalRate)
			.ratePerCategory(ratePerCategory)
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

	/**
	 * 사용자의 카테고리별 예산 비율을 계산한다.
	 *
	 * @param member 사용자
	 * @return 카테고리별 예산 비율
	 */
	private Map<CategoryName, Double> calculateCategoryRates(Member member) {
		int totalBudgetAmount = member.getMonthlyOverview().getTotalBudgetAmount();
		List<Budget> budgets = budgetRepository.findAllByMemberId(member.getId());
		return budgets.stream()
			.collect(Collectors.toMap(
				budget -> budget.getCategory().getName(),
				budget -> (double) budget.getAmount() / totalBudgetAmount
			));
	}

	/**
	 * 남은 예산을 이번 달의 남은 일수로 나누어 오늘 사용 가능한 지출 총액을 계산한다.
	 *
	 * @param member 사용자
	 * @return 오늘 사용 가능한 지출 총액
	 */
	private Integer calculateTodayBudget(Member member) {
		int totalBudgetAmount = member.getMonthlyOverview().getTotalBudgetAmount();
		int totalExpenditureAmount = member.getMonthlyOverview().getTotalExpenditureAmount();

		int remainingBudget = totalBudgetAmount - totalExpenditureAmount; // 남아있는 예산 총액
		int remainingDays = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth() + 1; // 이번 달의 남은 일수
		return remainingBudget / remainingDays;
	}

	/**
	 * 카테고리별 예산 비율을 기준으로 예산 총액을 나누어 카테고리별 지출액을 계산한다.
	 *
	 * @param categoryRates 카테고리별 예산 비율
	 * @param budget 예산 총액
	 * @return 카테고리별 지출액
	 */
	private Map<CategoryName, Integer> calculateAmountPerCategoryByRates(Map<CategoryName, Double> categoryRates,
		int budget) {
		return categoryRates.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> (int) (budget * entry.getValue()) / 100 * 100 // 100원 단위로 반올림
			));
	}

	/**
	 * 지출 추천 메시지를 생성한다.
	 * ((예산 총액 - 지출 총액) / 예산 총액)) / (남은 일수 / 해당 달의 총 일수)를 기준으로 삼아서 메시지를 생성한다.
	 *
	 * @param member 사용자
	 * @return 지출 추천 메시지
	 */
	private String createExpenditureMessage(Member member) {
		int totalBudgetAmount = member.getMonthlyOverview().getTotalBudgetAmount();
		int totalExpenditureAmount = member.getMonthlyOverview().getTotalExpenditureAmount();
		int remainingBudget = totalBudgetAmount - totalExpenditureAmount; // 남아있는 예산 총액
		int remainingDays = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth() + 1; // 이번 달의 남은 일수

		double moneyRate = (double) remainingBudget / totalBudgetAmount; // 예산 총액 대비 남아있는 예산 비율
		double dayRate = (double) remainingDays / LocalDate.now().lengthOfMonth(); // 이번 달의 남은 일수 대비 오늘의 비율
		double score = moneyRate / dayRate; // 지출 추천 점수

		// 지출 추천 점수가 어느정도인지에 따라 메시지를 생성한다.
		if (score >= GREAT.getScore()) {
			return GREAT.getMessage();
		}
		if (score >= GOOD.getScore()) {
			return GOOD.getMessage();
		}
		if (score >= NOT_BAD.getScore()) {
			return NOT_BAD.getMessage();
		}
		return OVER_SPENDING.getMessage();
	}

	/**
	 * 카테고리별 적정 금액, 지출금액의 차이인 위험도를 계산한다.
	 *
	 * @param amountPerCategory 카테고리별 지출 금액
	 * @param properAmountPerCategory 카테고리별 적정 금액
	 * @return 카테고리별 위험도 (퍼센티지)
	 */
	private Map<CategoryName, Integer> calculateDangerRates(Map<CategoryName, Integer> amountPerCategory,
		Map<CategoryName, Integer> properAmountPerCategory) {
		return amountPerCategory.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> (int) ((double) entry.getValue() / properAmountPerCategory.get(entry.getKey())) * 100
			));
	}
}
