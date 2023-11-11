package com.budgetguard.domain.expenditure.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpenditureMessage {

	GREAT("지출을 훌륭히 관리하고 있습니다!", 1.5),
	GOOD("지출을 잘 관리하고 있습니다.", 1.2),
	NOT_BAD("나쁘지 않은 편입니다.", 1.0),
	OVER_SPENDING("예산을 초과하셨습니다.", 0.0),
	;

	private final String message;
	private final double score;
}
