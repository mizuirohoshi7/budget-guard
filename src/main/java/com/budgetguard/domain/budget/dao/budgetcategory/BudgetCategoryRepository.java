package com.budgetguard.domain.budget.dao.budgetcategory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {

	Optional<BudgetCategory> findByName(CategoryName name);
}
