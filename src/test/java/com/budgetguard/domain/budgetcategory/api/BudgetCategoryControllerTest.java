package com.budgetguard.domain.budgetcategory.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.budgetguard.config.restdocs.AbstractRestDocsTest;

@WebMvcTest(BudgetCategoryController.class)
class BudgetCategoryControllerTest extends AbstractRestDocsTest {

	private static final String CATEGORY_URL = "/api/v1/budget-categories";

	@Nested
	@DisplayName("카테고리 목록 조회")
	class getBudgetCategories {
		@Test
		@DisplayName("카테고리 목록 조회 성공")
		void 카테고리_목록_조회_성공() throws Exception {
			mockMvc.perform(get(CATEGORY_URL))
				.andExpect(status().isOk());
		}
	}
}