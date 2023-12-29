package com.cyster.insight.impl.advisors;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.advisor.AdvisorTool;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.conversation.Conversation;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class WeatherAdvisor implements Advisor {
    public final String CODING_ADVISOR = "weather-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public WeatherAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return CODING_ADVISOR;
    }

    @Override
    public Conversation start() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(CODING_ADVISOR)
                .setInstructions("Get the current weather of a location")
                .withTool(new WeatherTool())
                .getOrCreate());
        }
        return this.advisor.get().start();
    }
    
    
    public static class WeatherTool implements AdvisorTool<Weather> {
        
        WeatherTool() {       
        }
        
        @Override
        public String getName() {
            return "get_weather";
        }

        @Override
        public String getDescription() {
            return "Get the current weather of a location";
        }

        @Override
        public Class<Weather> getParameterClass() {
            return Weather.class;
        }

        @Override
        public Object execute(Weather weather) {
            WeatherUnit unit = WeatherUnit.CELSIUS;
            if (weather.unit != null) {
                unit = weather.unit;
            }
            return new WeatherResponse(weather.location, unit, new Random().nextInt(50), "sunny");  
        }
    }
    
    public static class Weather {
        @JsonPropertyDescription("City and state, for example: Perth, Western Australia")
        @JsonProperty(required = true)
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'Celsius' or 'Fahrenheit'")
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
