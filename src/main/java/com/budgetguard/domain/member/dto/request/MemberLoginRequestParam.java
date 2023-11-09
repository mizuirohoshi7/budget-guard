package com.budgetguard.domain.member.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequestParam {

	private String account;
	private String password;

	@Builder
	private MemberLoginRequestParam(String account, String password) {
		this.account = account;
		this.password = password;
	}
}
