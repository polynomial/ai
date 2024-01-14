package com.extole.sage.advisors.jira;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class JiraWebClientBuilder {
    WebClient.Builder webClientBuilder;
    
    JiraWebClientBuilder(String baseJiraUrl) {
        this.webClientBuilder = WebClient.builder()
            .baseUrl(baseJiraUrl);
    }        
     
    public static JiraWebClientBuilder builder(String baseJiraUrl) {
        return new JiraWebClientBuilder(baseJiraUrl);
    }
    
    public JiraWebClientBuilder setApiKey(String apiKey) {
        if (apiKey.contains(":")) {
            String[] keyParts = apiKey.split(":");
            if (keyParts.length != 2) {
                throw new RuntimeException("Jira Key Bad");       
            }
            this.webClientBuilder.defaultHeaders(headers -> 
                headers.setBasicAuth(keyParts[0], keyParts[1], StandardCharsets.UTF_8 ));
        } else {   
            this.webClientBuilder.defaultHeaders(headers -> 
                headers.add("Authorization", "Bearer " + apiKey)); 
        }
        
        return this;
    }
    
    public JiraWebClientBuilder enableLogging() {
        this.webClientBuilder.filter(logRequest());
        this.webClientBuilder.filter(logResponse());
        
        return this;
    }
    
    
    public WebClient build() {
        return this.webClientBuilder.build();
    }
    
    private static ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> System.out.println(name + ":" + value)));
            return Mono.just(clientResponse);
        });
    }
}
