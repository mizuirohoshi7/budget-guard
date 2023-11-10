package com.budgetguard.domain.budget.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.dto.request.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetRecommendRequestParam;
import com.budgetguard.domain.budget.dto.request.BudgetUpdateRequestParam;
import com.budgetguard.domain.budget.dto.response.BudgetRecommendResponse;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.global.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

	static Budget budget;

	@InjectMocks
	BudgetService budgetService;

	@Mock
	BudgetRepository budgetRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	BudgetCategoryRepository budgetCategoryRepository;

	@BeforeEach
	void setUp() {
		budget = BudgetTestHelper.createBudget();
	}

	@Nested
	@DisplayName("예산 설정")
	class createBudget {
		@Test
		@DisplayName("예산 설정 성공")
		void 예산_설정_성공() {
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(budget.getMember().getId())
				.categoryName(budget.getCategory().getName().name())
				.amount(budget.getAmount())
				.build();

			given(memberRepository.findById(any())).willReturn(Optional.of(budget.getMember()));
			given(budgetCategoryRepository.findByName(any())).willReturn(Optional.of(budget.getCategory()));
			given(budgetRepository.save(any())).willReturn(budget);

			Long budgetId = budgetService.createBudget(param);

			assertThat(budgetId).isEqualTo(budget.getId());
			assertThat(budget.getMember().getMonthlyOverview().getTotalBudgetAmount()).isEqualTo(
				budget.getAmount()
			);
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 요청하면 실패")
		void 존재하지_않는_사용자로_요청하면_실패() {
			Long wrongId = 10L;
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(wrongId)
				.categoryName(budget.getCategory().getName().name())
				.amount(budget.getAmount())
				.build();

			given(memberRepository.findById(any())).willReturn(Optional.empty());

			assertThrows(BusinessException.class, () -> budgetService.createBudget(param));
		}

		@Test
		@DisplayName("존재하지 않는 카테고리로 요청하면 실패")
		void 존재하지_않는_카테고리로_요청하면_실패() {
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(budget.getMember().getId())
				.categoryName("Invalid Category")
				.amount(budget.getAmount())
				.build();

			given(memberRepository.findById(any())).willReturn(Optional.of(budget.getMember()));

			assertThrows(BusinessException.class, () -> budgetService.createBudget(param));
		}
	}

	@Nested
	@DisplayName("예산 수정")
	class updateBudget {
		@Test
		@DisplayName("예산 수정 성공")
		void 예산_수정_성공() {
			int updatedAmount = 9000;
			BudgetUpdateRequestParam param = BudgetUpdateRequestParam.builder()
				.memberId(budget.getMember().getId())
				.amount(updatedAmount)
				.build();

			given(budgetRepository.findById(any())).willReturn(Optional.of(budget));
			given(memberRepository.findById(any())).willReturn(Optional.of(budget.getMember()));

			budgetService.updateBudget(budget.getId(), param);

			assertThat(budget.getAmount()).isEqualTo(updatedAmount);
		}
	}

	@Nested
	@DisplayName("예산 추천")
	class recommendBudget {
		@Test
		@DisplayName("예산 추천 성공")
		void 예산_추천_성공() {
			BudgetRecommendRequestParam param = BudgetRecommendRequestParam.builder()
				.memberId(budget.getMember().getId())
				.totalBudgetAmount(30000)
				.build();

			given(memberRepository.findById(any())).willReturn(Optional.of(budget.getMember()));
			given(budgetCategoryRepository.findByName(any())).willReturn(Optional.of(budget.getCategory()));
			given(memberRepository.findAll()).willReturn(List.of(budget.getMember()));
			given(budgetRepository.findByMemberIdAndCategoryId(any(), any())).willReturn(Optional.of(budget));

			BudgetRecommendResponse budgetRecommendation = budgetService.createBudgetRecommendation(param);

			assertThat(budgetRecommendation).isNotNull();
		}
	}
}