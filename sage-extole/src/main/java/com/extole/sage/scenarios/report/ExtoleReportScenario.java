package com.extole.sage.scenarios.report;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.conversation.TooledChatConversation;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.cyster.sherpa.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.theokanning.openai.service.OpenAiService;
import com.extole.sage.scenarios.report.ExtoleReportScenario.Parameters;
import com.extole.sage.scenarios.report.ExtoleReportScenario.Context;


@Component
public class ExtoleReportScenario implements Scenario<Parameters, Context> {
    private static final String NAME = "extole-report";
    private OpenAiService openAiService;
    private ExtoleReportConfigurationToolFactory extolePersonToolFactory;

    ExtoleReportScenario(OpenAiService openAiService,
        ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory) {
        this.openAiService = openAiService;
        this.extolePersonToolFactory = extoleReportConfigurationToolFactory;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Describe an extole report given its report_id";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Conversation createConversation(Parameters parameters, Context context) {
        return new Builder(this.extolePersonToolFactory).setContext(context).start();
    }
    
    public class Builder {
        Context context;

        private ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory;

        Builder(ExtoleReportConfigurationToolFactory extoleReportConfigurationToolFactory) {
            this.extoleReportConfigurationToolFactory = extoleReportConfigurationToolFactory;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Conversation start() {
            String systemPrompt = "You are a customer service representative for the Extole SaaS marketing platform. You are looking at the report with id: {{report_id}}";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();

            Optional<String> accessToken = Optional.empty();
            accessToken = Optional.of(this.context.access_token);
            
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

    
    public static class Parameters {
        @JsonProperty(required = true)
        public String report_id;
    }
    
    public static class Context {
        @JsonProperty(required = true)
        public String access_token;
    }
}
