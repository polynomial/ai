package com.extole.sage.advisors.support;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

public class ExtoleWebClientBuilder {
    WebClient.Builder webClientBuilder;
    Optional<String> clientId = Optional.empty();
    Optional<String> superApiKey = Optional.empty();
    Optional<String> apiKey = Optional.empty();

    ExtoleWebClientBuilder(String baseUrl) {
        this.webClientBuilder = WebClient.builder()
            .baseUrl(baseUrl);
    }

    public static ExtoleWebClientBuilder builder(String baseUrl) {
        return new ExtoleWebClientBuilder(baseUrl);
    }

    public ExtoleWebClientBuilder setSuperApiKey(String superApiKey) {
        this.superApiKey = Optional.of(superApiKey);
        return this;
    }

    public ExtoleWebClientBuilder setApiKey(String apiKey) {
        this.apiKey = Optional.of(apiKey);
        return this;
    }

    public ExtoleWebClientBuilder setClientId(String clientId) {
        this.clientId = Optional.of(clientId);
        return this;
    }

    public ExtoleWebClientBuilder enableLogging() {
        this.webClientBuilder.filter(logRequest());
        this.webClientBuilder.filter(logResponse());

        return this;
    }

    public WebClient build() {
        Optional<String> key = Optional.empty();

        if (this.clientId.isEmpty()) {
            if (this.apiKey.isEmpty()) {
                if (this.superApiKey.isPresent()) {
                    key = this.superApiKey;
                    this.clientId = Optional.of("1890234003");
                } else {
                    throw new RuntimeException("No apiKey specified");
                }
            } else {
                key = this.apiKey;
            }
        } else {
            if (this.superApiKey.isPresent()) {
                key = Optional.of(getClientApiKey(this.superApiKey.get(), this.clientId.get()));
                this.apiKey = key;
            } else if (this.apiKey.isPresent()) {
                // TODO verify clientId == access token if unverified
                key = this.apiKey;
            } else {
                throw new RuntimeException("No apiKey specified");
            }
        }

        if (key.isPresent()) {
            final String bearer = key.get();
            this.webClientBuilder.defaultHeaders(headers -> headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearer));
        } else {
            throw new RuntimeException("No apiKey specified");
        }

        return this.webClientBuilder.build();
    }

    private String getClientApiKey(String superApiKey, String clientId) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("client_id", clientId);

        JsonNode response = this.webClientBuilder.build().post()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/tokens")
                .build())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + superApiKey)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (!response.path("access_token").isEmpty()) {
            throw new RuntimeException("Internal error, failed to obtain access_token for client");
        }
        return response.path("access_token").asText();

    }

    private static ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println("  " + name
                + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> System.out
                .println("  " + name + ":" + value)));
            return Mono.just(clientResponse);
        });
    }
}
