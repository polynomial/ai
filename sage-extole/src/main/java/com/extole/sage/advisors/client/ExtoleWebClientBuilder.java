package com.extole.sage.advisors.client;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class ExtoleWebClientBuilder {
    WebClient.Builder webClientBuilder;
    
    ExtoleWebClientBuilder(String baseJiraUrl) {
        this.webClientBuilder = WebClient.builder()
            .baseUrl(baseJiraUrl);
    }        
     
    public static ExtoleWebClientBuilder builder(String baseJiraUrl) {
        return new ExtoleWebClientBuilder(baseJiraUrl);
    }
    
    public ExtoleWebClientBuilder setApiKey(String apiKey) {
        this.webClientBuilder.defaultHeaders(headers -> 
                headers.add("Authorization", "Bearer " + apiKey)); 
        return this;
    }
    
    public ExtoleWebClientBuilder enableLogging() {
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
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println("  " + name + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> System.out.println("  " + name + ":" + value)));
            return Mono.just(clientResponse);
        });
    }
}
