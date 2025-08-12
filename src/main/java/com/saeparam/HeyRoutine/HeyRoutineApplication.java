package com.saeparam.HeyRoutine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing

public class HeyRoutineApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeyRoutineApplication.class, args);
	}

}
