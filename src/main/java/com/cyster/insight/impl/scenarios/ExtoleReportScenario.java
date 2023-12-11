package com.cyster.insight.impl.scenarios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.impl.conversation.TooledChatConversation;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


@Component
public class ExtoleReportScenario implements Scenario {
    private OpenAiFactoryImpl openAiFactory;
    private Map<String, String> defaultVariables = new HashMap<String, String>() {{
        put("report_id", "");
    }};

    ExtoleReportScenario(OpenAiFactoryImpl openAiFactory) {
        this.openAiFactory = openAiFactory;
    }

    @Override
    public String getName() {
        return "extole_report";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }


    @Override
    public ConversationBuilder createConversation() {
        return new Builder();
    }
    
    public class Builder implements Scenario.ConversationBuilder {
        Map<String, String> context = Collections.emptyMap();
        Optional<String> accessToken = Optional.empty();
            
        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder setAccessToken(String token) {
            this.accessToken = Optional.of(token);
            return this;
        }
        
        @Override
        public Conversation start() {
            String systemPrompt = "You are a customer service representative for the Extole SaaS marketing platform.";
    
            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();
                
            var conversation = new TooledChatConversation(openAiFactory)
                .addSystemMessage(messageWriter.toString())
                .addTool(
                    "get_report_configuration", "Get the configuration of a report given the report_id", ReportHandle.class,
                     reportHandle -> reportConfigurationLoader(this.accessToken, reportHandle));
    
            return new ReportConversation(conversation);
        }
    }

    public static class ReportHandle {
        @JsonPropertyDescription("The id of the report to load")
        public String id;
    }

    private static JsonNode reportConfigurationLoader(Optional<String> accessToken, ReportHandle reportHandle) {
        StringBuilder response = new StringBuilder();
        if (accessToken.isEmpty()) {
            response.append("{ \"error\": \"access_token_required\" }");
        } else {
            String jsonUrl = "https://api.extole.io/v4/reports/" + reportHandle.id;

            HttpURLConnection connection = null;
            try {
                URL url = new URL(jsonUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken.get());
                
                int responseCode = connection.getResponseCode();
    
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
    
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                } else {
                    response.append("{ \"error\": \"http_error\", \"code\": " + String.valueOf(responseCode) 
                        + ",\"content\":" + connection.getContent() + ", \"url\": \"" + url + "\"}");
                }
    
            } catch (IOException exception) {
                response.append("{ \"error\": \"http_read_error\", \"url\": \"" + jsonUrl + "\"}");
            } finally {
                connection.disconnect();
            }
        }
        
        JsonNode jsonNode;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(response.toString());
        } catch (JsonProcessingException exception) {
           throw new RuntimeException("Unable to parse Json response", exception);
        }
        
        return jsonNode; 
    }
    
    private static class ReportConversation implements Conversation {
        private TooledChatConversation conversation;
        private Boolean userMessage = false;

        ReportConversation(TooledChatConversation conversation) {
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
