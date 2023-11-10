package com.budgetguard.domain.expenditure.dao;

import java.util.List;

import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.entity.Expenditure;

/**
 * querydsl을 사용한 지출 조회용 레포지토리
 */
public interface ExpenditureQuerydslRepository {

	/**
	 * 검색 조건에 부합하는 지출 목록을 조회한다.
	 *
	 * @param searchCond 검색 조건
	 * @return 검색 조건에 부합하는 지출 목록
	 */
	List<Expenditure> searchExpenditures(ExpenditureSearchCond searchCond);
}
