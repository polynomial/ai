package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

public class ChatAdvisorConversation implements Conversation {
    private final String model = "gpt-3.5-turbo";

    private OpenAiService openAiService;
    private List<Message> messages;
    
    ChatAdvisorConversation(OpenAiService openAiService, List<Message> messages) {
        this.openAiService = openAiService;   
        this.messages = messages;
    }
    
    @Override
    public Conversation addMessage(String message) {
        this.messages.add(new Message(message));
        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        var chatMessages = new ArrayList<ChatMessage>();

        for (var message : this.messages) {
            if (message.getType() == Message.Type.SYSTEM) {
                chatMessages.add(new ChatMessage("system", message.getContent()));
            } else if (message.getType() == Message.Type.AI) {
                chatMessages.add(new ChatMessage("assistant", message.getContent()));
            } else if (message.getType() == Message.Type.USER) {
                chatMessages.add(new ChatMessage("user", message.getContent()));
            }
        }

        var chatCompletionRequest = ChatCompletionRequest.builder()
            .model(model)
            .messages(chatMessages)
            .build();

        var result = this.openAiService.createChatCompletion(chatCompletionRequest);

        var choices = result.getChoices();
        if (choices.size() == 0) {
            messages.add(new Message(Message.Type.INFO, "No responses"));
            throw new ConversationException("No Reponses");
        } 
        if (choices.size() > 1) {
            messages.add(new Message(Message.Type.INFO, "Multiple responses (ignored)"));
            throw new ConversationException("Multiple Reponses");
        }
        
        var message = new Message(Message.Type.AI, choices.get(0).getMessage().getContent());
        messages.add(message);
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            .filter(message -> message.getType() == Message.Type.AI || message.getType() == Message.Type.USER)
            .collect(Collectors.toList());
    }

}
