package com.example.webapp;

import com.example.webapp.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@SpringBootTest
class WebApplicationTests {

	UserController userController = new UserController();

	@Test
	void contextLoads() {
	}

	@Test
	void checkHealth() {
		assertEquals(new ResponseEntity<>(HttpStatus.OK), userController.healthz());
	}

	@Test
	void onePlusTest() {
		int sum = 2+2;
		assertEquals(sum,4);
	}

}
