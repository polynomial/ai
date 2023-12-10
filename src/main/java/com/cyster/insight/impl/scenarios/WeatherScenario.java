package com.cyster.insight.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.impl.conversation.TooledChatConversation;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class WeatherScenario implements Scenario {
    private OpenAiFactoryImpl openAiFactory;
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    WeatherScenario(OpenAiFactoryImpl openAiFactory) {
        this.openAiFactory = openAiFactory;
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
    public Conversation startConversation(Map<String, String> context) {
        String systemPrompt = "Get the current weather of a location";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, context);
        messageWriter.flush();

        var conversation = new TooledChatConversation(openAiFactory).addSystemMessage(messageWriter.toString()).addTool(
            "get_weather", "Get the current weather of a location", Weather.class,
            weather -> new WeatherResponse(weather.location, weather.unit, new Random().nextInt(50), "sunny"));

        return new WeatherConversation(conversation);
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
        private TooledChatConversation conversation;
        private Boolean userMessage = false;

        WeatherConversation(TooledChatConversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public void addMessage(String message) {
            this.conversation.addMessage(message);
        }

        @Override
        public Message respond() throws ConversationException {
            if (this.userMessage) {
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
