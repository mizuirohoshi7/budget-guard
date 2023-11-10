package com.budgetguard.domain.budget.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.budget.entity.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
