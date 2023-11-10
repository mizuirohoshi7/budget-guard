package com.budgetguard.global.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.budget.entity.budgetcategory.BudgetCategory;

import lombok.RequiredArgsConstructor;

/**
 * 더미 데이터 초기화용 클래스
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

	private final BudgetCategoryRepository budgetCategoryRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void initData() {
		initBudgetCategory();
	}

	/**
	 * 예산 카테고리 데이터를 초기화한다.
	 */
	private void initBudgetCategory() {
		for (CategoryName name : CategoryName.values()) {
			budgetCategoryRepository.save(BudgetCategory.builder().name(name).build());
		}
	}
}
