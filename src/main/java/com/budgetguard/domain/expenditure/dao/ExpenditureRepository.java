package com.budgetguard.domain.expenditure.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.expenditure.entity.Expenditure;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long>, ExpenditureQuerydslRepository {

	List<Expenditure> findAllByMemberId(Long memberId);
}
