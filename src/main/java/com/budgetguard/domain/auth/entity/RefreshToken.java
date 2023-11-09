package com.budgetguard.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

	@Id
	@Column(name = "refresh_token_key", nullable = false)
	private String key;

	@Column(name = "refresh_token_value", nullable = false)
	private String value;

	@Builder
	private RefreshToken(String key, String value) {
		this.key = key;
		this.value = value;
	}
}
