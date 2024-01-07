package com.cyster.sage.impl.scenarios;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sage.impl.advisors.WeatherAdvisor;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.cyster.sherpa.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class WeatherScenario implements Scenario {
    private Advisor advisor;
    
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    WeatherScenario(WeatherAdvisor weatherAdvisor) {
        this.advisor = weatherAdvisor;
    }

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new ConversationBuilder(this.advisor);
    }
    
    public class ConversationBuilder implements Scenario.ConversationBuilder {
        private Advisor advisor;
        private Map<String, String> context;
        
        ConversationBuilder(Advisor advisor) {
            this.advisor = advisor;
            this.context = Collections.emptyMap();
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public Conversation start() {
            return new WeatherConversation(this.advisor.createConversation().start(), this.context);
        }
    }

    
    public static class Weather {
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

    private static class WeatherConversation implements Conversation {
        private Conversation conversation;
        
        WeatherConversation(Conversation conversation, Map<String, String> context) {
            this.conversation = conversation;
        }

        @Override
        public WeatherConversation addMessage(String message) {
            this.conversation.addMessage(message);
            return this;
        }

        @Override
        public Message respond() throws ConversationException {
            List<Message> messages = this.conversation.getMessages();
            if (messages.size() == 0 || messages.get(messages.size() - 1).getType() != Message.Type.USER) {
                throw new ConversationException("This conversation scenaio requires a user prompt");
            }
            
            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }

    }

}
