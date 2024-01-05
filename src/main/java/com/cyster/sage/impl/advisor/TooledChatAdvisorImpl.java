package com.cyster.sage.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.sage.service.advisor.Advisor;
import com.cyster.sage.service.conversation.Conversation;
import com.cyster.sage.service.conversation.Message;
import com.theokanning.openai.service.OpenAiService;

public class TooledChatAdvisorImpl implements Advisor {

    private OpenAiService openAiService;
    private String name;
    private Toolset toolset;
    private List<Message> messages;
    
    TooledChatAdvisorImpl(OpenAiService openAiService, String name, Toolset toolset, List<Message> messages) {
        this.openAiService = openAiService;
        this.name = name;
        this.toolset = toolset;
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
            return new TooledChatAdvisorConversation(TooledChatAdvisorImpl.this.openAiService,
                TooledChatAdvisorImpl.this.toolset, TooledChatAdvisorImpl.this.messages);  
        }
    }
    
    static class Builder {
        private OpenAiService openAiService;
        private String name;
        private List<Message> messages;
        private Toolset.Builder toolsetBuilder;

        Builder(OpenAiService openAiService) {
            this.openAiService = openAiService;
            this.messages = new ArrayList<Message>();
            this.toolsetBuilder = new Toolset.Builder();
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
 
        public <T> Builder addTool(Tool<?> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }
        
        public TooledChatAdvisorImpl create() {
            return new TooledChatAdvisorImpl(openAiService, name, this.toolsetBuilder.create(), this.messages);
        }
    }
}
