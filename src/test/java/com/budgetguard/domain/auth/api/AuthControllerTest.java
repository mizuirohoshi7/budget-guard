package com.budgetguard.domain.auth.api;

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
import com.budgetguard.domain.auth.application.AuthService;
import com.budgetguard.domain.auth.dto.request.TokenRequest;
import com.budgetguard.domain.auth.dto.response.TokenResponse;
import com.budgetguard.domain.member.MemberTestHelper;
import com.budgetguard.domain.member.dto.request.MemberLoginRequestParam;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends AbstractRestDocsTest {

	static final String AUTH_URL = "/api/v1/auth";
	static final Member member = MemberTestHelper.createMember();

	@Autowired
	ObjectMapper mapper;

	@MockBean
	AuthService authService;

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

			mockMvc.perform(post(AUTH_URL + "/signup")
				.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isCreated());
		}

		@Test
		@DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 실패")
		void 비밀번호와_비밀번호_확인이_일치하지_않으면_실패() throws Exception {
			String wrongPasswordConfirm = "wrong";
			MemberSignupRequestParam param = MemberSignupRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.passwordConfirm(wrongPasswordConfirm)
				.build();

			mockMvc.perform(post(AUTH_URL + "/signup")
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

			mockMvc.perform(post(AUTH_URL + "/signup")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("로그인")
	class login {
		@Test
		@DisplayName("로그인 성공")
		void 로그인_성공() throws Exception {
			MemberLoginRequestParam param = MemberLoginRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.build();
			TokenResponse tokenResponse = TokenResponse.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

			given(authService.login(any())).willReturn(tokenResponse);

			mockMvc.perform(post(AUTH_URL + "/login")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("계정명과 비밀번호가 일치하지 않으면 실패")
		void 계정명과_비밀번호가_일치하지_않으면_실패() throws Exception {
			MemberLoginRequestParam param = MemberLoginRequestParam.builder()
				.account(member.getAccount())
				.password(member.getPassword())
				.build();

			given(authService.login(any())).willThrow(new BusinessException(param.getPassword(), "password", WRONG_PASSWORD));

			mockMvc.perform(post(AUTH_URL + "/login")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(param)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("토큰 재발급")
	class reissue {
		@Test
		@DisplayName("토큰 재발급 성공")
		void 토큰_재발급_성공() throws Exception {
			TokenRequest tokenRequest = TokenRequest.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();
			TokenResponse tokenResponse = TokenResponse.builder()
				.accessToken("new accessToken")
				.refreshToken("new refreshToken")
				.build();

			given(authService.reissue(any())).willReturn(tokenResponse);

			mockMvc.perform(post(AUTH_URL + "/reissue")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(tokenRequest)))
				.andExpect(status().isOk());
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 실패")
		void 유효하지_않은_토큰이면_실패() throws Exception {
			TokenRequest tokenRequest = TokenRequest.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

			given(authService.reissue(any())).willThrow(new BusinessException(tokenRequest.getRefreshToken(), "refreshToken", INVALID_REFRESH_TOKEN));

			mockMvc.perform(post(AUTH_URL + "/reissue")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(tokenRequest)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("로그아웃한 사용자면 실패")
		void 로그아웃한_사용자면_실패() throws Exception {
			TokenRequest tokenRequest = TokenRequest.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

			given(authService.reissue(any())).willThrow(new BusinessException("logout account", "account", MEMBER_LOGOUT));

			mockMvc.perform(post(AUTH_URL + "/reissue")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(tokenRequest)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("일치하지 않는 토큰이면 실패")
		void 일치하지_않는_토큰이면_실패() throws Exception {
			TokenRequest tokenRequest = TokenRequest.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

			given(authService.reissue(any())).willThrow(new BusinessException(tokenRequest.getRefreshToken(), "refreshToken", REFRESH_TOKEN_MISMATCH));

			mockMvc.perform(post(AUTH_URL + "/reissue")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(tokenRequest)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("로그아웃")
	class logout {
		@Test
		@DisplayName("로그아웃 성공")
		void 로그아웃_성공() throws Exception {
			TokenRequest tokenRequest = TokenRequest.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build();

			mockMvc.perform(post(AUTH_URL + "/logout")
					.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(tokenRequest)))
				.andExpect(status().isOk());
		}
	}
}