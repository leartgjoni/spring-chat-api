package com.leartgjoni.springchatapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes =  {HealthController.class})
class HealthControllerTest {
    @Autowired
    HealthController controller;

    @Test
    void health() {
        assertEquals("healthy", controller.health());
    }
}