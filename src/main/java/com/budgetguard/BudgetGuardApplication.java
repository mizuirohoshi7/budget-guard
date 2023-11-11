package com.budgetguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BudgetGuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetGuardApplication.class, args);
	}

}
