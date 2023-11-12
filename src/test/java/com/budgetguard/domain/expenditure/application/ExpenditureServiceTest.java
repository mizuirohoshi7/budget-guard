package com.budgetguard.domain.expenditure.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.budgetguard.domain.budget.BudgetTestHelper;
import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.expenditure.ExpenditureTestHelper;
import com.budgetguard.domain.expenditure.dao.ExpenditureRepository;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRateResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRecommendationResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSearchResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureTodayResponse;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.budgetguard.domain.member.dao.MemberRepository;

@ExtendWith(MockitoExtension.class)
class ExpenditureServiceTest {

	static Expenditure expenditure;

	@InjectMocks
	ExpenditureService expenditureService;

	@Mock
	ExpenditureRepository expenditureRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	BudgetCategoryRepository budgetCategoryRepository;

	@Mock
	BudgetRepository budgetRepository;

	@BeforeEach
	void setUp() {
		expenditure = ExpenditureTestHelper.createExpenditure();
	}

	@Nested
	@DisplayName("지출 생성")
	class createExpenditure {
		@Test
		@DisplayName("지출 생성 성공")
		void 지출_생성_성공() {
			ExpenditureCreateRequestParam param = ExpenditureCreateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.categoryName(expenditure.getBudgetCategory().getName().name())
				.amount(expenditure.getAmount())
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();
			given(memberRepository.findById(anyLong())).willReturn(Optional.of(expenditure.getMember()));
			given(budgetCategoryRepository.findByName(any())).willReturn(Optional.of(expenditure.getBudgetCategory()));
			given(expenditureRepository.save(any())).willReturn(expenditure);

			Long savedExpenditureId = expenditureService.createExpenditure(param);

			assertThat(savedExpenditureId).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 수정")
	class updateExpenditure {
		@Test
		@DisplayName("지출 수정 성공")
		void 지출_수정_성공() {
			ExpenditureUpdateRequestParam param = ExpenditureUpdateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.amount(3000)
				.memo("updated memo")
				.isExcluded(true)
				.build();
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));
			given(memberRepository.findById(anyLong())).willReturn(Optional.of(expenditure.getMember()));

			Long updatedExpenditureId = expenditureService.updateExpenditure(expenditure.getId(), param);

			assertThat(updatedExpenditureId).isNotNull();
			assertThat(expenditure.getAmount()).isEqualTo(3000);
			assertThat(expenditure.getMemo()).isEqualTo("updated memo");
			assertThat(expenditure.getIsExcluded()).isTrue();
		}
	}

	@Nested
	@DisplayName("지출 상세 조회")
	class getExpenditure {
		@Test
		@DisplayName("지출 상세 조회 성공")
		void 지출_상세_조회_성공() {
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));

			ExpenditureDetailResponse expenditureDetail = expenditureService.getExpenditure(expenditure.getId());

			assertThat(expenditureDetail).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 삭제")
	class deleteExpenditure {
		@Test
		@DisplayName("지출 삭제 성공")
		void 지출_삭제_성공() {
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));

			Long deletedExpenditureId = expenditureService.deleteExpenditure(expenditure.getId());

			assertThat(deletedExpenditureId).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 검색 목록 조회")
	class getExpenditures {
		@Test
		@DisplayName("지출 검색 목록 조회 성공")
		void 지출_검색_목록_조회_성공() {
			ExpenditureSearchCond searchCond = ExpenditureSearchCond.builder()
				.memberId(expenditure.getMember().getId())
				.startDate(LocalDate.of(2021, 1, 1))
				.endDate(LocalDate.of(2021, 1, 31))
				.categoryName(CategoryName.FOOD.name())
				.minAmount(1000)
				.maxAmount(5000)
				.build();
			given(expenditureRepository.searchExpenditures(any())).willReturn(List.of(expenditure));

			ExpenditureSearchResponse searchResponse = expenditureService.searchExpenditures(searchCond);

			assertThat(searchResponse).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 추천")
	class createExpenditureRecommendation {
		@Test
		@DisplayName("지출 추천 성공")
		void 지출_추천_성공() {
			given(memberRepository.findByAccount(any())).willReturn(Optional.of(expenditure.getMember()));
			given(budgetRepository.findAllByMemberId(any())).willReturn(List.of(BudgetTestHelper.createBudget()));

			ExpenditureRecommendationResponse expenditureRecommendation = expenditureService.createExpenditureRecommendation(
				expenditure.getMember().getAccount());

			assertThat(expenditureRecommendation).isNotNull();
		}
	}

	@Nested
	@DisplayName("오늘 지출 생성")
	class createExpenditureToday {
		@Test
		@DisplayName("오늘 지출 생성 성공")
		void 오늘_지출_생성_성공() {
			given(memberRepository.findByAccount(any())).willReturn(Optional.of(expenditure.getMember()));
			given(expenditureRepository.findAllByMemberId(any())).willReturn(List.of(expenditure));
			given(budgetRepository.findAllByMemberId(any())).willReturn(List.of(BudgetTestHelper.createBudget()));

			ExpenditureTodayResponse expenditureToday = expenditureService.createExpenditureToday(
				expenditure.getMember().getAccount());

			assertThat(expenditureToday).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 통계")
	class createExpenditureRate {
		@Test
		@DisplayName("지난 달 대비 통계 생성 성공")
		void 지난_달_대비_통계_생성_성공() {
			given(memberRepository.findByAccount(any())).willReturn(Optional.of(expenditure.getMember()));
			given(expenditureRepository.findAllByMemberId(any())).willReturn(List.of(expenditure));

			ExpenditureRateResponse expenditureRate = expenditureService.createExpenditureMonthlyRate(
				expenditure.getMember().getAccount());

			assertThat(expenditureRate).isNotNull();
		}

		@Test
		@DisplayName("지난 요일 대비 통계 생성 성공")
		void 지난_요일_대비_통계_생성_성공() {
			given(memberRepository.findByAccount(any())).willReturn(Optional.of(expenditure.getMember()));
			given(expenditureRepository.findAllByMemberId(any())).willReturn(List.of(expenditure));

			ExpenditureRateResponse expenditureRate = expenditureService.createExpenditureDayOfWeekRate(
				expenditure.getMember().getAccount());

			assertThat(expenditureRate).isNotNull();
		}

		@Test
		@DisplayName("다른 사용자 대비 통계 생성 성공")
		void 다른_사용자_대비_통계_생성_성공() {
			given(memberRepository.findByAccount(any())).willReturn(Optional.of(expenditure.getMember()));
			given(expenditureRepository.findAllByMemberId(any())).willReturn(List.of(expenditure));

			ExpenditureRateResponse expenditureRate = expenditureService.createOtherMemberExpenditureRate(
				expenditure.getMember().getAccount());

			assertThat(expenditureRate).isNotNull();
		}
	}
}