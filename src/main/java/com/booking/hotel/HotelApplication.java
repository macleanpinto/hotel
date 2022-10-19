package com.booking.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotelApplication {

	public static void main(String[] args) {
		WarmingUpHealthCheck warmingUpHealthCheck = new WarmingUpHealthCheck();
		warmingUpHealthCheck.run();
		SpringApplication.run(HotelApplication.class, args);

	}

}
