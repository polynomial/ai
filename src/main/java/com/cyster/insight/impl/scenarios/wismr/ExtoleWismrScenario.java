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
    private ExtolePersonStepsToolFactory extolePersonStepsToolFactory;
    private ExtoleStepsToolFactory extoleStepsToolFactory;

    private Map<String, String> defaultVariables = new HashMap<String, String>();

    ExtoleWismrScenario(OpenAiFactoryImpl openAiFactory,
        ExtolePersonFindToolFactory extolePersonFindToolFactory,
        ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory,
        ExtolePersonStepsToolFactory extolePersonStepsToolFactory,
        ExtoleStepsToolFactory extoleStepsToolFactory) {
        this.openAiFactory = openAiFactory;
        this.extolePersonFindToolFactory = extolePersonFindToolFactory;
        this.extolePersonRewardsToolFactory = extolePersonRewardsToolFactory;
        this.extolePersonStepsToolFactory = extolePersonStepsToolFactory;
        this.extoleStepsToolFactory = extoleStepsToolFactory;
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
        return new Builder(this.extolePersonFindToolFactory,
            this.extolePersonRewardsToolFactory,
            this.extolePersonStepsToolFactory,
            this.extoleStepsToolFactory);
    }

    public class Builder implements Scenario.ConversationBuilder {
        Map<String, String> context = Collections.emptyMap();
        Optional<String> accessToken = Optional.empty();

        private ExtolePersonFindToolFactory extolePersonFindToolFactory;
        private ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory;
        private ExtolePersonStepsToolFactory extolePersonStepsToolFactory;
        private ExtoleStepsToolFactory extoleStepsToolFactory;

        Builder(ExtolePersonFindToolFactory extolePersonFindToolFactory,
            ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory,
            ExtolePersonStepsToolFactory extolePersonStepsToolFactory,
            ExtoleStepsToolFactory extoleStepsToolFactory) {
            this.extolePersonFindToolFactory = extolePersonFindToolFactory;
            this.extolePersonRewardsToolFactory = extolePersonRewardsToolFactory;
            this.extolePersonStepsToolFactory = extolePersonStepsToolFactory;
            this.extoleStepsToolFactory = extoleStepsToolFactory;
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
            String systemPrompt = """
You are a customer service representative for the Extole SaaS marketing platform.
You specialize in helping people find a reward they expected to receive from the Extole platform.

Step 1: Identify the person and check if they have any rewards. 
The best way to start is to try and load and review the persons profile base on a key we might have, 
such as email, partner_user_id or order_id.

Step 2: Person has rewards
If the person has rewards, show the rewards, we are done.

Step 3: Person has no rewards
Get all the stepNames that have an actionName earnRewards.

Step 4:
Check if the person has have any of the stepNames on their profile that earn rewards.
If there are steps, show the steps and we are done.

""";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();

            var conversation = new TooledChatConversation(openAiFactory)
                .addSystemMessage(messageWriter.toString())
                .addTool(this.extolePersonFindToolFactory.create(accessToken))
                .addTool(this.extolePersonRewardsToolFactory.create(accessToken))
                .addTool(this.extolePersonStepsToolFactory.create(accessToken))
                .addTool(this.extoleStepsToolFactory.create(accessToken));

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

            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }
    }

}
