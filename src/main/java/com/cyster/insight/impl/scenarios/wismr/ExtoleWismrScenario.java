package com.cyster.insight.impl.scenarios.wismr;

import java.io.StringReader;
import java.io.StringWriter;
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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class ExtoleWismrScenario implements Scenario {
    private OpenAiFactoryImpl openAiFactory;
    private ExtolePersonFindToolFactory extolePersonFindToolFactory;
    private ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory;

    private Map<String, String> defaultVariables = new HashMap<String, String>();

    ExtoleWismrScenario(OpenAiFactoryImpl openAiFactory,
        ExtolePersonFindToolFactory extolePersonFindToolFactory,
        ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory) {
        this.openAiFactory = openAiFactory;
        this.extolePersonFindToolFactory = extolePersonFindToolFactory;
        this.extolePersonRewardsToolFactory = extolePersonRewardsToolFactory;
    }

    @Override
    public String getName() {
        return "extole_wismr";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new Builder(this.extolePersonFindToolFactory, this.extolePersonRewardsToolFactory);
    }

    public class Builder implements Scenario.ConversationBuilder {
        Map<String, String> context = Collections.emptyMap();
        Optional<String> accessToken = Optional.empty();

        private ExtolePersonFindToolFactory extolePersonFindToolFactory;
        private ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory;

        Builder(ExtolePersonFindToolFactory extolePersonFindToolFactory,
            ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory) {
            this.extolePersonFindToolFactory = extolePersonFindToolFactory;
            this.extolePersonRewardsToolFactory = extolePersonRewardsToolFactory;
        }

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
            String systemPrompt = "You are a customer service representative for the Extole SaaS marketing platform."
                + "You specialize in helping people find a reward they expected to recieve from the Extole platform "
                + "The best way to start is to try and load and review the persons profile base on a key we might have, "
                + "such as email, partner_user_id or order_id. "
                + "When you have found the person, the first thing to check is if we think they have earned any rewards. ";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();

            var conversation = new TooledChatConversation(openAiFactory)
                .addSystemMessage(messageWriter.toString())
                .addTool(this.extolePersonFindToolFactory.create(accessToken))
                .addTool(this.extolePersonRewardsToolFactory.create(accessToken));

            return new WismrConversation(conversation);
        }
    }

    private static class WismrConversation implements Conversation {
        private TooledChatConversation conversation;
        private Boolean userMessage = false;

        WismrConversation(TooledChatConversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public WismrConversation addMessage(String message) {
            this.conversation.addMessage(message);
            return this;
        }

        @Override
        public Message respond() throws ConversationException {
            if (this.userMessage) {
                throw new ConversationException("This conversation scenaio requires a user prompt");
            }
            System.out.println("call ExtoleWismrRespond");
            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }
    }

}
