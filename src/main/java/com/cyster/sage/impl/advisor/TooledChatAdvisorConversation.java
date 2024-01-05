package com.cyster.sage.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cyster.sage.service.conversation.Conversation;
import com.cyster.sage.service.conversation.ConversationException;
import com.cyster.sage.service.conversation.Message;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.service.OpenAiService;

public class TooledChatAdvisorConversation implements Conversation {
    private final String model = "gpt-3.5-turbo-0613";

    private OpenAiService openAiService;
    private Toolset toolset;
    private List<Message> messages;
    
    TooledChatAdvisorConversation(OpenAiService openAiService, Toolset toolset, List<Message> messages) {
        this.openAiService = openAiService;   
        this.toolset = toolset;
        this.messages = messages;
    }
    
    @Override
    public Conversation addMessage(String message) {
        this.messages.add(new Message(message));
        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        Message response = null;
    
        while (response == null) {
            var chatMessages = new ArrayList<ChatMessage>();
        
            // TOOD "get_weather" hard coded?
        
            for (var message : this.messages) {
                switch (message.getType()) {
                case SYSTEM:
                    chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), message.getContent()));
                    break;
                case AI:
                    chatMessages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), message.getContent()));
                    break;
                case USER:
                    chatMessages.add(new ChatMessage(ChatMessageRole.USER.value(), message.getContent()));
                    break;
                case FUNCTION_CALL:
                    chatMessages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), message.getContent(),
                        "get_weather"));
                case FUNCTION_RESULT:
                    chatMessages.add(new ChatMessage(ChatMessageRole.FUNCTION.value(), message.getContent(),
                        "get_weather"));
                    break;
                default:
                    // ignore
                }
            }
        
            var chatFunctionToolset = new ChatFunctionToolset(this.toolset);
            var chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(chatMessages)
                .functions(chatFunctionToolset.getFunctions())
                .functionCall(new ChatCompletionRequestFunctionCall("auto"))
                .maxTokens(1000)
                .build();
    
            var chatResponse = this.openAiService.createChatCompletion(chatCompletionRequest);
    
            var choices = chatResponse.getChoices();
            if (choices.size() > 1) {
                messages.add(new Message(Message.Type.INFO, "Multiple responses (ignored, only taking 1st response)"));
            }
            var choice = choices.get(0);
    
            switch (choice.getFinishReason()) {
            case "stop":
                var messageContent = choice.getMessage().getContent();
                response = new Message(Message.Type.AI, messageContent);
                messages.add(response);
                break;
    
            case "length":
                messages.add(new Message(Message.Type.ERROR, "Token Limit Exceeded"));
                break;
    
            case "content_filter":
                messages.add(new Message(Message.Type.ERROR, "Content Filtered"));
                break;
    
            case "function_call":
                ChatFunctionCall functionCall = choice.getMessage().getFunctionCall();
                if (functionCall == null) {
                    messages.add(new Message(Message.Type.ERROR, "Function call specified, but not found"));
                }
                messages.add(new Message(Message.Type.FUNCTION_CALL, functionCall.getName() + "(" + functionCall
                    .getArguments()
                    + ")"));
    
                ChatMessage functionResponseMessage = chatFunctionToolset.call(functionCall);
                messages.add(new Message(Message.Type.FUNCTION_RESULT, functionResponseMessage.getContent()));
                break;
    
            default:
                messages.add(new Message(Message.Type.ERROR, "Unexpected finish reason: " + choice.getFinishReason()));
            }
        }
    
        return response;      
    }

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            .filter(message -> message.getType() == Message.Type.AI || message.getType() == Message.Type.USER)
            .collect(Collectors.toList());
    }

}
