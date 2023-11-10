package com.budgetguard.domain.budget.api;

import static com.budgetguard.global.error.ErrorCode.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.budgetguard.config.restdocs.AbstractRestDocsTest;
import com.budgetguard.domain.budget.BudgetTestHelper;
import com.budgetguard.domain.budget.application.BudgetService;
import com.budgetguard.domain.budget.dto.BudgetCreateRequestParam;
import com.budgetguard.domain.budget.entity.Budget;
import com.budgetguard.global.error.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest extends AbstractRestDocsTest {

	static final String BUDGET_URL = "/api/v1/budgets";
	static final Budget budget = BudgetTestHelper.createBudget();

	@Autowired
	ObjectMapper mapper;

	@MockBean
	BudgetService budgetService;

	@Nested
	@DisplayName("예산 설정")
	class createBudget {
		@Test
		@DisplayName("예산 설정 성공")
		void 예산_설정_성공() throws Exception {
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(budget.getId())
				.categoryName(budget.getCategory().getName().name())
				.amount(budget.getAmount())
				.build();

			given(budgetService.createBudget(any())).willReturn(budget.getId());

			mockMvc.perform(post(BUDGET_URL)
				.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 요청하면 실패")
		void 존재하지_않는_사용자로_요청하면_실패() throws Exception {
			Long wrongId = 10L;
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(wrongId)
				.categoryName(budget.getCategory().getName().name())
				.amount(budget.getAmount())
				.build();

			given(budgetService.createBudget(any())).willThrow(
				new BusinessException(param.getMemberId(), "memberId", MEMBER_NOT_FOUND)
			);

			mockMvc.perform(post(BUDGET_URL)
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("존재하지 않는 카테고리로 요청하면 실패")
		void 존재하지_않는_카테고리로_요청하면_실패() throws Exception {
			BudgetCreateRequestParam param = BudgetCreateRequestParam.builder()
				.memberId(budget.getId())
				.categoryName("Invalid Category")
				.amount(budget.getAmount())
				.build();

			given(budgetService.createBudget(any())).willThrow(
				new BusinessException(param.getCategoryName(), "categoryName", BUDGET_CATEGORY_NOT_FOUND)
			);

			mockMvc.perform(post(BUDGET_URL)
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}
	}
}