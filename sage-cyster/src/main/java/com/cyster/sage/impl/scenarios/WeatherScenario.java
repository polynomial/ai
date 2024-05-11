package com.cyster.sage.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.assistant.service.advisor.Advisor;
import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.sage.impl.advisors.WeatherAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.cyster.sage.impl.scenarios.WeatherScenario.Parameters;

@Component
public class WeatherScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "weather";
    
    private Advisor<Void> advisor;
    
    WeatherScenario(WeatherAdvisor weatherAdvisor) {
        this.advisor = weatherAdvisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Gives a weather at a location";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Parameters parameters, Void context) {
        return this.advisor.createConversation().start();
    }
    
    public static class Parameters {
        @JsonPropertyDescription("City and state, for example: León, Guanajuato")
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @JsonProperty(required = true)
        public WeatherUnit unit;
    }

    public enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

    public static class WeatherResponse {
        public String location;
        public WeatherUnit unit;
        public int temperature;
        public String description;

        public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.description = description;
        }
    }

}
