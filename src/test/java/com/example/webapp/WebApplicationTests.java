package com.example.webapp;

import com.example.webapp.controller.UserAccountController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@SpringBootTest
class WebappApplicationTests {

	UserAccountController userAccountController = new UserAccountController();

	@Test
	void contextLoads() {
	}

	@Test
	void checkHealth() {
		assertEquals(new ResponseEntity<>(HttpStatus.OK), userAccountController.healthz());
	}

	@Test
	void mathTest() {
		int sum = 1+1;
		assertEquals(sum, 2);
	}

}
