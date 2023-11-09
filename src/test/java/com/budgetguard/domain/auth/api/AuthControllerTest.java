package com.budgetguard.domain.auth.api;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.member.MemberTestHelper;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

	static final String URL = "/api/v1/auth";
	static final Member member = MemberTestHelper.createMember();

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	@MockBean
	AuthService authService;

	@BeforeEach
	void setUp(WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Nested
	@DisplayName("회원 가입")
	class signup {
		@Test
		@DisplayName("회원 가입 성공")
		void 회원_가입_성공() throws Exception {
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(member.getPassword())
				.build();

			given(authService.signup(any())).willReturn(1L);

			mockMvc.perform(post(URL + "/signup")
				.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isCreated());
		}

		@Test
		@DisplayName("비밀번호와 비밀번호 확인이 일치하지않으면 실패")
		void 비밀번호와_비밀번호_확인이_일치하지않으면_실패() throws Exception {
			String wrongPasswordConfirm = "wrong";
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(wrongPasswordConfirm)
				.build();

			mockMvc.perform(post(URL + "/signup")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("이미 사용중인 계정명이면 실패")
		void 이미_사용중인_계정명이면_실패() throws Exception {
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(member.getPassword())
				.build();

			given(authService.signup(any())).willThrow(new BusinessException(member.getAccount(), "account", ErrorCode.DUPLICATED_ACCOUNT));

			mockMvc.perform(post(URL + "/signup")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}
	}
}