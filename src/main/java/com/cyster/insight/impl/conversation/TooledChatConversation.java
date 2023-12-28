package com.cyster.insight.impl.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.impl.advisor.AdvisorTool;
import com.cyster.insight.impl.advisor.Toolset;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.Message;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class TooledChatConversation implements Conversation {

    private final String model = "gpt-3.5-turbo-0613";

    private OpenAiFactoryImpl openAiFactory;
    private List<Message> messages;
    private Toolset.Builder toolsetBuilder;

    public TooledChatConversation(OpenAiFactoryImpl openAiFactory) {
        this.openAiFactory = openAiFactory;
        this.messages = new ArrayList<Message>();
        this.toolsetBuilder = new Toolset.Builder();
    }

    @Override
    public TooledChatConversation addMessage(String content) {
        this.messages.add(new Message(content));

        return this;
    }


    public TooledChatConversation addUserMessage(String content) {
        this.messages.add(new Message(content));
        return this;
    }
    
    public TooledChatConversation addSystemMessage(String content) {
        this.messages.add(new Message(Message.Type.SYSTEM, content));
        return this;
    }
    
    public TooledChatConversation addAiMessage(String content) {
        this.messages.add(new Message(Message.Type.AI, content));
        return this;
    }
    public <T> TooledChatConversation addTool(String name, String description, Class<T> parameterClass,
        Function<T, Object> executor) {
        var tool = new ChatToolPojo<T>(name, description, parameterClass, executor);
        return this.addTool(tool);
    }

    public <T> TooledChatConversation addTool(AdvisorTool<?> tool) {
        this.toolsetBuilder.addTool(tool);
        return this;
    }

    @Override
    public Message respond() {
        Message response = null;

        while (response == null) {
            var chatMessages = new ArrayList<ChatMessage>();

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

            Toolset toolset = this.toolsetBuilder.create();
            
            // debug
            toolset.printSchema();
            
            var chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(chatMessages)
                .functions(toolset.getFunctions())
                .functionCall(new ChatCompletionRequestFunctionCall("auto"))
                .maxTokens(1000)
                .build();

            var chatResponse = this.openAiFactory.getService().createChatCompletion(chatCompletionRequest);

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

                ChatMessage functionResponseMessage = toolset.call(functionCall);
                messages.add(new Message(Message.Type.FUNCTION_RESULT, functionResponseMessage.getContent()));
                break;

            default:
                messages.add(new Message(Message.Type.ERROR, "Unexpected finish reason: " + choice.getFinishReason()));
            }
        }

        return response;
    }

    private static class ChatToolPojo<T> implements AdvisorTool<T> {
        private String name;
        private String description;
        private Class<T> parameterClass;
        private Function<T, Object> executor;

        public ChatToolPojo(String name, String description, Class<T> parameterClass, Function<T, Object> executor) {
            this.name = name;
            this.description = description;
            this.parameterClass = parameterClass;
            this.executor = executor;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Class<T> getParameterClass() {
            return this.parameterClass;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object execute(Object parameters) {
            return this.getExecutor().apply((T)parameters);   
        }
        
        @Override
        public Function<T, Object> getExecutor() {
            return this.executor;
        }

    }

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            // .filter(message -> message.getType() == Message.Type.AI || message.getType()
            // == Message.Type.USER)
            .collect(Collectors.toList());
    }

}