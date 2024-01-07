package com.cyster.sherpa.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.Message;
import com.theokanning.openai.service.OpenAiService;

public class ChatAdvisorImpl implements Advisor {

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

    
    public class ConversationBuilder implements Advisor.ConversationBuilder {
        Optional<String> overrideInstructions = Optional.empty();
        
        private ConversationBuilder() {    
        }

        public ConversationBuilder setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }
        
        @Override
        public Conversation start() {
            // TODO implement overrideInstruction
            return new ChatAdvisorConversation(ChatAdvisorImpl.this.openAiService, ChatAdvisorImpl.this.messages);  
        }
    }
    
    
    static class Builder {
        private OpenAiService openAiService;
        private String name;
        private List<Message> messages;

        Builder(OpenAiService openAiService) {
            this.openAiService = openAiService;
            this.messages = new ArrayList<Message>();
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder addUserMessage(String content) {
            this.messages.add(new Message(content));
            return this;
        }

        public Builder addSystemMessage(String content) {
            this.messages.add(new Message(Message.Type.SYSTEM, content));
            return this;
        }

        public Builder addAiMessage(String content) {
            this.messages.add(new Message(Message.Type.AI, content));
            return this;
        }
 
        public ChatAdvisorImpl create() {
            return new ChatAdvisorImpl(openAiService, name, this.messages);
        }
    }
}
