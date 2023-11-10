package com.budgetguard.domain.expenditure.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.expenditure.entity.Expenditure;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
}
