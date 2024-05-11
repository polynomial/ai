package com.cyster.assistant.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.assistant.service.advisor.Advisor;
import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.conversation.Message;
import com.theokanning.openai.service.OpenAiService;

public class ChatAdvisorImpl<C> implements Advisor<C> {

    private OpenAiService openAiService;
    private String name;
    private List<Message> messages;

    ChatAdvisorImpl(OpenAiService openAiService, String name, List<Message> messages) {
        this.openAiService = openAiService;
        this.name = name;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ConversationBuilder createConversation() {
        return new ConversationBuilder();
    }

    public class ConversationBuilder implements Advisor.ConversationBuilder<C> {
        Optional<String> overrideInstructions = Optional.empty();
        C context = null;

        private ConversationBuilder() {
        }

        @Override
        public ConversationBuilder withContext(C context) {
            this.context = context;
            return this;
        }

        public ConversationBuilder setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }

        @Override
        public com.cyster.assistant.service.advisor.Advisor.ConversationBuilder<C> addMessage(String message) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Conversation start() {
            // TODO implement overrideInstruction
            return new ChatAdvisorConversation(ChatAdvisorImpl.this.openAiService, ChatAdvisorImpl.this.messages);
        }
    }

    static class Builder<C2> {
        private OpenAiService openAiService;
        private String name;
        private List<Message> messages;

        Builder(OpenAiService openAiService) {
            this.openAiService = openAiService;
            this.messages = new ArrayList<Message>();
        }

        public Builder<C2> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<C2> addUserMessage(String content) {
            this.messages.add(new Message(content));
            return this;
        }

        public Builder<C2> addSystemMessage(String content) {
            this.messages.add(new Message(Message.Type.SYSTEM, content));
            return this;
        }

        public Builder<C2> addAiMessage(String content) {
            this.messages.add(new Message(Message.Type.AI, content));
            return this;
        }

        public ChatAdvisorImpl<C2> create() {
            return new ChatAdvisorImpl<C2>(openAiService, name, this.messages);
        }
    }
}
