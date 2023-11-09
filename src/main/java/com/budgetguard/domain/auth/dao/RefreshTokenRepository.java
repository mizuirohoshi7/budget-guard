package com.budgetguard.domain.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
