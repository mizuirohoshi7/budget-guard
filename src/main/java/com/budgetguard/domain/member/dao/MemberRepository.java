package com.budgetguard.domain.member.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByAccount(String account);
}
