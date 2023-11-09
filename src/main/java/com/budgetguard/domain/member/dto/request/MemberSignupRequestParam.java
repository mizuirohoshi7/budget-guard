package com.budgetguard.domain.member.dto.request;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.budgetguard.domain.member.entity.Member;
import com.budgetguard.global.error.BusinessException;
import com.budgetguard.global.error.ErrorCode;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignupRequestParam {

	private static final int MIN_ACCOUNT_LENGTH = 6;
	private static final int MAX_ACCOUNT_LENGTH = 20;

	// 대소문자와 특수문자로 구성된 8~16 자리의 문자
	private static final String PASSWORD_REGEX_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";

	@NotEmpty(message = "계정명을 입력해주세요.")
	@Length(min = MIN_ACCOUNT_LENGTH, max = MAX_ACCOUNT_LENGTH, message = "계정명을 {min} ~ {max} 사이로 입력해주세요.")
	private String account;

	@NotEmpty(message = "비밀번호를 입력해주세요.")
	@Pattern(regexp = PASSWORD_REGEX_PATTERN, message = "비밀번호는 특수문자를 포함한 8~16 자리의 문자여야 합니다.")
	private String password;

	@NotEmpty(message = "비밀번호 확인을 입력해주세요.")
	@Pattern(regexp = PASSWORD_REGEX_PATTERN, message = "비밀번호는 특수문자를 포함한 8~16 자리의 문자여야 합니다.")
	private String passwordConfirm;

	@Builder
	private MemberSignupRequestParam(String account, String password, String passwordConfirm) {
		this.account = account;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	/**
	 * 비밀번호와 비밀번호 확인이 일치하지 않으면 예외 처리
	 */
	public void isValidPassword() {
		if (!password.equals(passwordConfirm)) {
			throw new BusinessException(password, "password", ErrorCode.INVALID_PASSWORD_CONFIRM);
		}
	}

	public Member toEntity(PasswordEncoder passwordEncoder) {
		return Member.builder()
				.account(account)
				.password(passwordEncoder.encode(password))
				.build();
	}
}
