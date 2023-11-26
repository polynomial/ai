package com.cyster.aiapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import 

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Hello world\n";
	}

    
    @PostMapping("/hello")
    public String hello() {
        return "Hello world";
        OpenAiService service = new OpenAiService("your_token");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("Somebody once told me the world is gonna roll me")
                .model("ada")
                .echo(true)
                .build();
        service.createCompletion(completionRequest).getChoices().forEach(System.out::println);
    }
}
