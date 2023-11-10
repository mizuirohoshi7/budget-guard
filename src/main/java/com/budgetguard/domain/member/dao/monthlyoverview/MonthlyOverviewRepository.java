package com.budgetguard.domain.member.dao.monthlyoverview;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetguard.domain.member.entity.monthlyoverview.MonthlyOverview;

public interface MonthlyOverviewRepository extends JpaRepository<MonthlyOverview, Long> {
}
