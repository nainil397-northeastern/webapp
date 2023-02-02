package com.example.webapp.tests;

import com.example.webapp.controller.UserController;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class ControllerTest {

    UserController userController = new UserController();

    @Test
    public void checkHealth() {
        assertEquals(new ResponseEntity<>(HttpStatus.NO_CONTENT), userController.healthz());
    }

    @Test
    public void onePlusTest() {
        int sum = 1+1;
        assertEquals(sum, 3);
    }

}
