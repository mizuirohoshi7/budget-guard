package com.budgetguard.domain.expenditure.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.budgetguard.config.restdocs.AbstractRestDocsTest;
import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.expenditure.ExpenditureTestHelper;
import com.budgetguard.domain.expenditure.application.ExpenditureService;
import com.budgetguard.domain.expenditure.constant.ExpenditureMessage;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRateResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureRecommendationResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSearchResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureSimpleResponse;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureTodayResponse;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.budgetguard.global.config.security.TokenManager;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;
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

	@MockBean
	TokenManager tokenManager;

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

	@Nested
	@DisplayName("지출 상세 조회")
	class getExpenditure {
		@Test
		@DisplayName("지출 상세 조회 성공")
		void 지출_상세_조회_성공() throws Exception {
			ExpenditureDetailResponse expenditureDetail = new ExpenditureDetailResponse(expenditure);
			given(expenditureService.getExpenditure(any())).willReturn(expenditureDetail);

			mockMvc.perform(get(EXPENDITURE_URL + "/" + expenditure.getId())
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("존재하지 않는 지출을 조회하면 실패")
		void 존재하지_않는_지출을_조회하면_실패() throws Exception {
			int wrongExpenditureId = 100;
			given(expenditureService.getExpenditure(any())).willThrow(new BusinessException(wrongExpenditureId, "expenditureId", ErrorCode.EXPENDITURE_NOT_FOUND));

			mockMvc.perform(get(EXPENDITURE_URL + "/" + wrongExpenditureId)
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("지출 삭제")
	class deleteExpenditure {
		@Test
		@DisplayName("지출 삭제 성공")
		void 지출_삭제_성공() throws Exception {
			given(expenditureService.getExpenditure(any())).willReturn(new ExpenditureDetailResponse(expenditure));
			given(expenditureService.deleteExpenditure(any())).willReturn(expenditure.getId());

			mockMvc.perform(delete(EXPENDITURE_URL + "/" + expenditure.getId())
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("존재하지 않는 지출을 삭제하면 실패")
		void 존재하지_않는_지출을_삭제하면_실패() throws Exception {
			int wrongExpenditureId = 100;
			given(expenditureService.getExpenditure(any())).willReturn(new ExpenditureDetailResponse(expenditure));
			given(expenditureService.deleteExpenditure(any())).willThrow(new BusinessException(wrongExpenditureId, "expenditureId", ErrorCode.EXPENDITURE_NOT_FOUND));

			mockMvc.perform(delete(EXPENDITURE_URL + "/" + wrongExpenditureId)
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("지출 목록 검색")
	class searchExpenditures {
		@Test
		@DisplayName("지출 목록 검색 성공")
		void 지출_목록_검색_성공() throws Exception {
			MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
			param.add("memberId", "1");
			param.add("startDate", "2020-01-01");
			param.add("endDate", "2020-01-31");
			param.add("categoryName", "FOOD");
			param.add("minAmount", "1000");
			param.add("maxAmount", "10000");

			Map<CategoryName, Integer> amountPerCategory = Map.of(
				CategoryName.FOOD, 5000,
				CategoryName.TRANSPORTATION, 3000,
				CategoryName.ENTERTAINMENT, 2000
			);
			ExpenditureSearchResponse searchResponse = ExpenditureSearchResponse.builder()
				.expenditures(List.of(new ExpenditureSimpleResponse(expenditure)))
				.totalAmount(10000)
				.amountPerCategory(amountPerCategory)
				.build();

			given(expenditureService.searchExpenditures(any())).willReturn(searchResponse);

			mockMvc.perform(get(EXPENDITURE_URL)
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
					.params(param)
				)
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("지출 추천")
	class createExpenditureRecommendation {
		@Test
		@DisplayName("지출 추천 성공")
		void 지출_추천_성공() throws Exception {
			given(tokenManager.getAccountFromToken(any())).willReturn("account");
			ExpenditureRecommendationResponse expenditureRecommendation = ExpenditureRecommendationResponse.builder()
				.totalAmount(10000)
				.amountPerCategory(Map.of(
					CategoryName.FOOD, 5000,
					CategoryName.TRANSPORTATION, 3000,
					CategoryName.ENTERTAINMENT, 2000
				))
				.expenditureMessage(ExpenditureMessage.GREAT.getMessage())
				.build();
			given(expenditureService.createExpenditureRecommendation(any())).willReturn(expenditureRecommendation);

			mockMvc.perform(get(EXPENDITURE_URL + "/recommendation")
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("오늘 지출 생성")
	class createExpenditureToday {
		@Test
		@DisplayName("오늘 지출 생성 성공")
		void 오늘_지출_생성_성공() throws Exception {
			given(tokenManager.getAccountFromToken(any())).willReturn("account");
			ExpenditureTodayResponse expenditureToday = ExpenditureTodayResponse.builder()
				.totalAmount(10000)
				.amountPerCategory(Map.of(
					CategoryName.FOOD, 5000,
					CategoryName.TRANSPORTATION, 3000,
					CategoryName.ENTERTAINMENT, 2000
				))
				.properTotalAmount(20000)
				.properAmountPerCategory(Map.of(
					CategoryName.FOOD, 10000,
					CategoryName.TRANSPORTATION, 6000,
					CategoryName.ENTERTAINMENT, 4000
				))
				.dangerRates(Map.of(
					CategoryName.FOOD, 50,
					CategoryName.TRANSPORTATION, 50,
					CategoryName.ENTERTAINMENT, 50
				))
				.build();
			given(expenditureService.createExpenditureToday(any())).willReturn(expenditureToday);

			mockMvc.perform(get(EXPENDITURE_URL + "/today")
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("지출 통계")
	class createExpenditureRate {
		@Test
		@DisplayName("지난 달 대비 통계 생성 성공")
		void 지난_달_대비_통계_생성_성공() throws Exception {
			ExpenditureRateResponse expenditureRate = ExpenditureRateResponse.builder()
				.totalRate(50)
				.ratePerCategory(Map.of(
					CategoryName.FOOD, 50,
					CategoryName.TRANSPORTATION, 50,
					CategoryName.ENTERTAINMENT, 50
				))
				.build();
			given(tokenManager.getAccountFromToken(any())).willReturn("account");
			given(expenditureService.createExpenditureRate(any())).willReturn(expenditureRate);

			mockMvc.perform(get(EXPENDITURE_URL + "/monthly-rate")
					.contentType(APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, JWT_TOKEN)
				)
				.andExpect(status().isOk());
		}
	}
}