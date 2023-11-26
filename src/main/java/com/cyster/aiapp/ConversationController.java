package com.cyster.aiapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConversationController {

    @GetMapping("/conversations")
    public String index() {
        return "Hello world";
    }

}
