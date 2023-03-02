package com.example.webapp.tests;

import com.example.webapp.controller.UserAccountController;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class ControllerTest {

    UserAccountController userAccountController = new UserAccountController();

    @Test
    public void checkHealth() {
        assertEquals(new ResponseEntity<>(HttpStatus.NO_CONTENT), userAccountController.healthz());
    }

    @Test
    public void onePlusTest() {
        int sum = 1+1;
        assertEquals(sum, 3);
    }

}