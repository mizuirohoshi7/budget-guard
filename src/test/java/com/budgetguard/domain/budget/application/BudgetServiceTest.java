package com.budgetguard.domain.budget.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.budgetguard.domain.budget.BudgetTestHelper;
import com.budgetguard.domain.budget.dao.BudgetRepository;
import com.budgetguard.domain.budget.dto.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.domain.budgetcategory.dao.BudgetCategoryRepository;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.global.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

	static final Budget budget = BudgetTestHelper.createBudget();

	@InjectMocks
	BudgetService budgetService;

	@Mock
	BudgetRepository budgetRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	BudgetCategoryRepository budgetCategoryRepository;

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
			given(budgetCategoryRepository.findByName(any())).willReturn(Optional.empty());

			assertThrows(BusinessException.class, () -> budgetService.createBudget(param));
		}
	}
}