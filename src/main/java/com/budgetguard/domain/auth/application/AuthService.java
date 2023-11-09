package com.budgetguard.domain.auth.application;

import static com.budgetguard.global.error.ErrorCode.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetguard.domain.auth.dao.RefreshTokenRepository;
import com.budgetguard.domain.auth.dto.request.TokenRequest;
import com.budgetguard.domain.auth.dto.response.TokenResponse;
import com.budgetguard.domain.auth.entity.RefreshToken;
import com.budgetguard.domain.member.dao.MemberRepository;
import com.budgetguard.domain.member.dto.request.MemberLoginRequestParam;
import com.budgetguard.domain.member.dto.request.MemberSignupRequestParam;
import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.config.security.TokenManager;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManagerBuilder authenticationManagerBuilder; // 로그인 검증 관리자
	private final TokenManager tokenManager; // JWT 토큰 관리자
	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 회원 가입
	 *
	 * @param param 회원 가입 입력 데이터
	 * @return 생성된 회원의 id
	 */
	public Long signup(MemberSignupRequestParam param) {
		checkDuplicatedAccount(param.getAccount());

		Member member = param.toEntity(passwordEncoder);
		Member savedMember = memberRepository.save(member);
		return savedMember.getId();
	}

	/**
	 * 회원 아이디 중복 확인
	 * 중복이면 예외 처리
	 *
	 * @param account 확인할 회원 아이디
	 */
	private void checkDuplicatedAccount(String account) {
		if (memberRepository.findByAccount(account).isPresent()) {
			throw new BusinessException(account, "account", ErrorCode.DUPLICATED_ACCOUNT);
		}
	}

	/**
	 * 로그인 성공 시 JWT 토큰 생성
	 *
	 * @param param 로그인 입력 데이터
	 * @return JWT 토큰
	 */
	public TokenResponse login(MemberLoginRequestParam param) {
		// 1. 로그인 정보로 AuthenticationToken 생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			param.getAccount(), param.getPassword());

		// 2. 비밀번호 검증
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

		// 3. 인증 정보를 기반으로 JWT 토큰 생성
		TokenResponse tokenResponse = tokenManager.createTokenResponse(authentication);

		// 4. RefreshToken 저장
		RefreshToken refreshToken = RefreshToken.builder()
			.key(authentication.getName())
			.value(tokenResponse.getRefreshToken())
			.build();
		refreshTokenRepository.save(refreshToken);

		return tokenResponse;
	}

	/**
	 * 토큰 재발급
	 *
	 * @param tokenRequest accessToken, refreshToken
	 * @return 재발급된 accessToken, refreshToken을 담은 tokenResponse
	 */
	public TokenResponse reissue(TokenRequest tokenRequest) {
		// Refresh Token 검증
		if (tokenManager.validateToken(tokenRequest.getRefreshToken())) {
			throw new BusinessException(tokenRequest.getRefreshToken(), "refreshToken", INVALID_REFRESH_TOKEN);
		}

		// Access Token에서 사용자 정보 추출
		Authentication authentication = tokenManager.createAuthentication(tokenRequest.getAccessToken());

		// 계정명으로 저장된 Refresh Token을 조회
		RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName()).orElseThrow(
			() -> new BusinessException(authentication.getName(), "account", MEMBER_LOGOUT)
		);

		// 요청으로 받은 Refresh Token과 저장된 Refresh Token 일치 여부 확인
		if (!refreshToken.getValue().equals(tokenRequest.getRefreshToken())) {
			throw new BusinessException(tokenRequest.getRefreshToken(), "refreshToken", REFRESH_TOKEN_MISMATCH);
		}

		// 토큰 재발급
		TokenResponse tokenResponse = tokenManager.createTokenResponse(authentication);

		// 기존 Refresh Token 삭제 후 갱신된 Refresh Token 저장
		refreshTokenRepository.delete(refreshToken);
		RefreshToken newRefreshToken = RefreshToken.builder()
			.key(authentication.getName())
			.value(tokenResponse.getRefreshToken())
			.build();
		refreshTokenRepository.save(newRefreshToken);

		return tokenResponse;
	}

	/**
	 * 로그아웃
	 *
	 * @param tokenRequest accessToken, refreshToken
	 */
	public void logout(TokenRequest tokenRequest) {
		// Refresh Token 검증
		if (!tokenManager.validateToken(tokenRequest.getRefreshToken())) {
			throw new BusinessException(tokenRequest.getRefreshToken(), "refreshToken", INVALID_REFRESH_TOKEN);
		}

		// Access Token에서 사용자 정보 추출
		Authentication authentication = tokenManager.createAuthentication(tokenRequest.getAccessToken());

		// 계정명으로 저장된 Refresh Token을 조회
		RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName()).orElseThrow(
			() -> new BusinessException(authentication.getName(), "account", MEMBER_LOGOUT)
		);

		refreshTokenRepository.delete(refreshToken);
	}
}
