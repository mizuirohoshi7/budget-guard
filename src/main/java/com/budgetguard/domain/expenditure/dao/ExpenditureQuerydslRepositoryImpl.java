package com.budgetguard.domain.expenditure.dao;

import static com.budgetguard.domain.budget.entity.budgetcategory.QBudgetCategory.*;
import static com.budgetguard.domain.expenditure.entity.QExpenditure.*;
import static com.budgetguard.domain.member.entity.QMember.*;

import java.time.LocalDate;
import java.util.List;

import com.budgetguard.domain.budget.constant.CategoryName;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureSearchCond;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

/**
 * querydsl을 사용한 지출 조회용 레포지토리 구현체
 * 인터페이스 이름 + Impl 형식으로 클래스를 생성해야 스프링에 자동으로 등록된다.
 */
public class ExpenditureQuerydslRepositoryImpl implements ExpenditureQuerydslRepository {

	private final JPAQueryFactory query;

	public ExpenditureQuerydslRepositoryImpl(EntityManager em) {
		this.query = new JPAQueryFactory(em);
	}

	/**
	 * 검색 조건에 부합하는 지출을 조회한다.
	 *
	 * @param searchCond 검색 조건
	 * @return 검색 조건에 부합하는 지출 리스트
	 */
	@Override
	public List<Expenditure> searchExpenditures(ExpenditureSearchCond searchCond) {
		return query
			.select(expenditure)
			.from(expenditure)
			.join(expenditure.member, member).fetchJoin()
			.join(expenditure.budgetCategory, budgetCategory).fetchJoin()

			// 필수가 아닌 검색 조건은 null 일 경우 무시하는 동적 검색
			.where(
				isMemberIdEqual(searchCond.getMemberId()),
				isCreatedDateBetween(searchCond.getStartDate(), searchCond.getEndDate()),
				isCategoryEqual(searchCond.getCategoryName()),
				isAmountBetween(searchCond.getMinAmount(), searchCond.getMaxAmount())
			)

			.fetch();
	}

	/**
	 * 해당 사용자의 지출만을 조회한다.
	 *
	 * @param memberId 사용자 ID
	 * @return 해당 사용자의 지출 리스트
	 */
	private BooleanExpression isMemberIdEqual(Long memberId) {
		return expenditure.member.id.eq(memberId);
	}

	/**
	 * 검색 시작일과 종료일 사이의 지출을 조회한다.
	 *
	 * @param startDate 검색 시작일
	 * @param endDate 검색 종료일
	 * @return 검색 시작일과 종료일 사이의 지출 리스트
	 */
	private BooleanExpression isCreatedDateBetween(LocalDate startDate, LocalDate endDate) {
		return expenditure.createdTime.between(startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1).minusNanos(1));
	}

	/**
	 * 검색 조건에 categoryName이 포함되어 있으면 해당 카테고리만 조회한다.
	 * categoryName이 null이면 모든 카테고리를 조회한다.
	 *
	 * @param categoryName 카테고리 이름
	 * @return 카테고리 이름이 일치하는 지출 리스트
	 */
	private BooleanExpression isCategoryEqual(String categoryName) {
		// categoryName이 null이면 모든 카테고리를 조회한다.
		if (categoryName == null) {
			return null;
		}
		return budgetCategory.name.eq(CategoryName.of(categoryName));
	}

	/**
	 * 검색 조건에 최소 금액과 최대 금액 각각의 포함 여부에 따라 지출을 조회한다.
	 *
	 * @param minAmount 최소 금액
	 * @param maxAmount 최대 금액
	 * @return 최소 금액과 최대 금액 각각의 포함 여부에 따라 지출 리스트
	 */
	private BooleanExpression isAmountBetween(Integer minAmount, Integer maxAmount) {

		// minAmount와 maxAmount가 모두 null이면 모든 지출액을 조회한다.
		if (minAmount == null && maxAmount == null) {
			return null;
		}

		// minAmount만 null이면 지출액이 maxAmount 이하인 지출을 조회한다.
		if (maxAmount == null) {
			return expenditure.amount.goe(minAmount);
		}

		// maxAmount만 null이면 지출액이 minAmount 이상인 지출을 조회한다.
		if (minAmount == null) {
			return expenditure.amount.loe(maxAmount);
		}

		// minAmount와 maxAmount가 모두 null이 아니면 지출액이 minAmount 이상 maxAmount 이하인 지출을 조회한다.
		return expenditure.amount.between(minAmount, maxAmount);
	}
}
