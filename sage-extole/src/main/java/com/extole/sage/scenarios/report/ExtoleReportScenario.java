package com.extole.sage.scenarios.report;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.conversation.TooledChatConversation;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.cyster.sherpa.service.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.theokanning.openai.service.OpenAiService;

@Component
public class ExtoleReportScenario implements Scenario {
    private OpenAiService openAiService;
    private ExtoleReportConfigurationToolFactory extolePersonToolFactory;

    private Map<String, String> defaultVariables = new HashMap<String, String>() {
        {
            put("report_id", "");
        }
    };

    ExtoleReportScenario(OpenAiService openAiService,
        ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory) {
        this.openAiService = openAiService;
        this.extolePersonToolFactory = extoleReportConfigurationToolFactory;
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
        return new Builder(this.extolePersonToolFactory);
    }

    public class Builder implements Scenario.ConversationBuilder {
        Map<String, String> context = Collections.emptyMap();

        private ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory;

        Builder(ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory) {
            this.extoleReportConfigurationToolFactory = extoleReportConfigurationToolFactory;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public Conversation start() {
            String systemPrompt = "You are a customer service representative for the Extole SaaS marketing platform. You are looking at the report with id: {{report_id}}";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();

            Optional<String> accessToken = Optional.empty();
            if (this.context.containsKey("access_token")) {
                accessToken = Optional.of(this.context.get("access_token"));
            }
            
            var conversation = new TooledChatConversation(openAiService)
                .addSystemMessage(messageWriter.toString())
                .addTool(this.extoleReportConfigurationToolFactory.create(accessToken));

            return new ReportConversation(conversation);
        }
    }

    private static class ReportConversation implements Conversation {
        private TooledChatConversation conversation;
        private Boolean userMessage = false;

        ReportConversation(TooledChatConversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public ReportConversation addMessage(String message) {
            this.conversation.addMessage(message);
            return this;
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
