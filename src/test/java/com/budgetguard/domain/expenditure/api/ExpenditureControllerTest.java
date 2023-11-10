package com.budgetguard.domain.expenditure.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

import com.budgetguard.config.restdocs.AbstractRestDocsTest;
import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.expenditure.ExpenditureTestHelper;
import com.budgetguard.domain.expenditure.application.ExpenditureService;
import com.budgetguard.domain.expenditure.dto.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ExpenditureController.class)
class ExpenditureControllerTest extends AbstractRestDocsTest {

	static final String EXPENDITURE_URL = "/api/v1/expenditures";
	static final String JWT_TOKEN = "JWT_TOKEN";

	static Expenditure expenditure;

	@MockBean
	ExpenditureService expenditureService;

	@MockBean
	AuthService authService;

	@Autowired
	ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		expenditure = ExpenditureTestHelper.createExpenditure();
	}

	@Nested
	@DisplayName("지출 생성")
	class createExpenditure {
		@Test
		@DisplayName("지출 생성 성공")
		void 지출_생성_성공() throws Exception {
			ExpenditureCreateRequestParam param = ExpenditureCreateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.categoryName(expenditure.getBudgetCategory().getName().name())
				.amount(expenditure.getAmount())
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();

			given(expenditureService.createExpenditure(any())).willReturn(expenditure.getId());

			mockMvc.perform(post(EXPENDITURE_URL)
					.contentType(APPLICATION_JSON)
					.content(mapper.writeValueAsString(param))
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("지출을 음수로 생성하면 실패")
		void 지출을_음수로_생성하면_실패() throws Exception {
			Integer wrongAmount = -1000;
			ExpenditureCreateRequestParam param = ExpenditureCreateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.categoryName(expenditure.getBudgetCategory().getName().name())
				.amount(wrongAmount)
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();

			mockMvc.perform(post(EXPENDITURE_URL)
					.contentType(APPLICATION_JSON)
					.content(mapper.writeValueAsString(param))
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("지출 수정")
	class updateExpenditure {
		@Test
		@DisplayName("지출 수정 성공")
		void 지출_수정_성공() throws Exception {
			ExpenditureUpdateRequestParam param = ExpenditureUpdateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.amount(expenditure.getAmount())
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();

			given(expenditureService.updateExpenditure(any(), any())).willReturn(expenditure.getId());

			mockMvc.perform(put(EXPENDITURE_URL + "/" + expenditure.getId())
					.contentType(APPLICATION_JSON)
					.content(mapper.writeValueAsString(param))
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("지출을 음수로 수정하면 실패")
		void 지출을_음수로_수정하면_실패() throws Exception {
			Integer wrongAmount = -1000;
			ExpenditureUpdateRequestParam param = ExpenditureUpdateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.amount(wrongAmount)
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();

			mockMvc.perform(put(EXPENDITURE_URL + "/" + expenditure.getId())
					.contentType(APPLICATION_JSON)
					.content(mapper.writeValueAsString(param))
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isBadRequest());
		}
	}
}