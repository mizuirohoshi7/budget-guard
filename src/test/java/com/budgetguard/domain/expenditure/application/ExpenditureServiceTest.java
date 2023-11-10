package com.budgetguard.domain.expenditure.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.budgetguard.domain.budget.dao.budgetcategory.BudgetCategoryRepository;
import com.budgetguard.domain.expenditure.ExpenditureTestHelper;
import com.budgetguard.domain.expenditure.dao.ExpenditureRepository;
import com.budgetguard.domain.expenditure.dto.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.entity.Expenditure;
import com.budgetguard.domain.member.dao.MemberRepository;

@ExtendWith(MockitoExtension.class)
class ExpenditureServiceTest {

	static Expenditure expenditure;

	@InjectMocks
	ExpenditureService expenditureService;

	@Mock
	ExpenditureRepository expenditureRepository;

	@Mock
	MemberRepository memberRepository;

	@Mock
	BudgetCategoryRepository budgetCategoryRepository;

	@BeforeEach
	void setUp() {
		expenditure = ExpenditureTestHelper.createExpenditure();
	}

	@Nested
	@DisplayName("지출 생성")
	class createExpenditure {
		@Test
		@DisplayName("지출 생성 성공")
		void 지출_생성_성공() {
			ExpenditureCreateRequestParam param = ExpenditureCreateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.categoryName(expenditure.getBudgetCategory().getName().name())
				.amount(expenditure.getAmount())
				.memo(expenditure.getMemo())
				.isExcluded(expenditure.getIsExcluded())
				.build();

			given(memberRepository.findById(anyLong())).willReturn(Optional.of(expenditure.getMember()));
			given(budgetCategoryRepository.findByName(any())).willReturn(Optional.of(expenditure.getBudgetCategory()));
			given(expenditureRepository.save(any())).willReturn(expenditure);

			Long savedExpenditureId = expenditureService.createExpenditure(param);

			assertThat(savedExpenditureId).isNotNull();
		}
	}
}