package com.example.rph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RecordProcessorHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordProcessorHandlerApplication.class, args);
	}

}
