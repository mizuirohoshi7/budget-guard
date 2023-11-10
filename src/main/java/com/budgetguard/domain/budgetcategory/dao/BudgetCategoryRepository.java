package com.budgetguard.domain.budgetcategory.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.budgetcategory.entity.BudgetCategory;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {

	Optional<BudgetCategory> findByName(String name);
}
