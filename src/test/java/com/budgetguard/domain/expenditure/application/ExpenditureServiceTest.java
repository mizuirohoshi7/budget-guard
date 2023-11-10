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
import com.budgetguard.domain.expenditure.dto.request.ExpenditureCreateRequestParam;
import com.budgetguard.domain.expenditure.dto.request.ExpenditureUpdateRequestParam;
import com.budgetguard.domain.expenditure.dto.response.ExpenditureDetailResponse;
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

	@Nested
	@DisplayName("지출 수정")
	class updateExpenditure {
		@Test
		@DisplayName("지출 수정 성공")
		void 지출_수정_성공() {
			ExpenditureUpdateRequestParam param = ExpenditureUpdateRequestParam.builder()
				.memberId(expenditure.getMember().getId())
				.amount(3000)
				.memo("updated memo")
				.isExcluded(true)
				.build();
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));
			given(memberRepository.findById(anyLong())).willReturn(Optional.of(expenditure.getMember()));

			Long updatedExpenditureId = expenditureService.updateExpenditure(expenditure.getId(), param);

			assertThat(updatedExpenditureId).isNotNull();
			assertThat(expenditure.getAmount()).isEqualTo(3000);
			assertThat(expenditure.getMemo()).isEqualTo("updated memo");
			assertThat(expenditure.getIsExcluded()).isTrue();
		}
	}

	@Nested
	@DisplayName("지출 상세 조회")
	class getExpenditure {
		@Test
		@DisplayName("지출 상세 조회 성공")
		void 지출_상세_조회_성공() {
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));

			ExpenditureDetailResponse expenditureDetail = expenditureService.getExpenditure(expenditure.getId());

			assertThat(expenditureDetail).isNotNull();
		}
	}

	@Nested
	@DisplayName("지출 삭제")
	class deleteExpenditure {
		@Test
		@DisplayName("지출 삭제 성공")
		void 지출_삭제_성공() {
			given(expenditureRepository.findById(anyLong())).willReturn(Optional.of(expenditure));

			Long deletedExpenditureId = expenditureService.deleteExpenditure(expenditure.getId());

			assertThat(deletedExpenditureId).isNotNull();
		}
	}
}