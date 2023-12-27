package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.List;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.Message;
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
    public Conversation start() {
        return new ChatAdvisorConversation(this.openAiService, this.messages); 
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
