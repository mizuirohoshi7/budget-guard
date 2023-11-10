package com.budgetguard.domain.budget.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.budgetguard.domain.budget.entity.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

	@Query("SELECT b "
		+ "FROM Budget b "
		+ "LEFT JOIN FETCH b.category "
		+ "WHERE b.member.id =:memberId "
		+ "AND b.category.id =:categoryId")
	Optional<Budget> findByMemberIdAndCategoryId(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId);
}
