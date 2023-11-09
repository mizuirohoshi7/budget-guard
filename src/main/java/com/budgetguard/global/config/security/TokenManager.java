package com.budgetguard.global.config.security;

import static java.lang.System.*;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.budgetguard.domain.auth.dto.response.TokenResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 정보로 JWT 토큰을 만들거나 토큰을 바탕으로 사용자 정보를 가져온다.
 */
@Slf4j
@Component
public class TokenManager {

	private static final String AUTHORITIES_KEY = "auth";
	private static final String AUTHORITIES_SEPARATOR = ",";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

	private final Key key;

	/**
	 * 암호화 키 생성
	 *
	 * @param secretKey propertise에서 관리하는 키
	 */
	public TokenManager(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * 사용자 정보를 넘겨받아서 Access Token과 Refresh Token을 생성
	 *
	 * @param authentication 사용자 정보
	 * @return JWT 토큰 Dto
	 */
	public TokenResponse createTokenResponse(Authentication authentication) {
		// 권한 불러오기
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(AUTHORITIES_SEPARATOR));

		// Access Token 생성
		Date accessTokenExpiration = new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME);
		String accessToken = Jwts.builder()
			.setSubject(authentication.getName())
			.claim(AUTHORITIES_KEY, authorities)
			.setExpiration(accessTokenExpiration)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// Refresh Token 생성
		Date refreshTokenExpiration = new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME);
		String refreshToken = Jwts.builder()
			.setExpiration(refreshTokenExpiration)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		return TokenResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	/**
	 * 토큰을 복호화해서 사용자 정보를 추출한다.
	 *
	 * @param token JWT 토큰
	 * @return 사용자 정보
	 */
	public Authentication createAuthentication(String token) {
		Claims claims = toClaims(token);

		// 클레임에서 권한 정보 추출
		List<SimpleGrantedAuthority> authorities = Arrays.stream(
				claims.get(AUTHORITIES_KEY).toString().split(AUTHORITIES_SEPARATOR))
			.map(SimpleGrantedAuthority::new)
			.toList();

		// 권한 정보를 기반으로 UserDetails 객체를 생성한다.
		User principal = new User(claims.getSubject(), "", authorities);

		// 사용자 정보를 기반으로 사용자 정보를 생성한다.
		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	/**
	 * 토큰을 복호화해서 클레임 정보를 추출한다.
	 *
	 * @param token JWT 토큰
	 * @return 클레임 정보
	 */
	private Claims toClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.debug("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.debug("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.debug("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.debug("JWT 토큰이 잘못되었습니다.");
		}
		return false;
	}
}
