package com.cyster.insight.impl.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.FunctionExecutor;

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
    public void addMessage(String content) {
        this.messages.add(new Message(content));
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

    public <T> TooledChatConversation addTool(ChatTool<?> tool) {
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
                case FUNCTION:
                    chatMessages.add(new ChatMessage(ChatMessageRole.FUNCTION.value(), message.getContent(),
                        "get_weather"));
                    break;
                default:
                    // ignore
                }
            }

            Toolset toolset = this.toolsetBuilder.create();

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
                messages.add(new Message(Message.Type.AI, functionCall.getName() + "(" + functionCall.getArguments()
                    + ")"));

                ChatMessage functionResponseMessage = toolset.call(functionCall);
                messages.add(new Message(Message.Type.FUNCTION, functionResponseMessage.getContent()));
                break;

            default:
                messages.add(new Message(Message.Type.ERROR, "Unexpected finish reason: " + choice.getFinishReason()));
            }
        }

        return response;
    }

    private static class ChatToolPojo<T> implements ChatTool<T> {
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

        @Override
        public Function<T, Object> getExecutor() {
            return this.executor;
        }

    }

    public static class Toolset {
        private FunctionExecutor functionExecutor;

        private Toolset(List<ChatTool<?>> tools) {
            var functions = new ArrayList<ChatFunction>();

            for (var tool : tools) {
                functions.add(chatTooltoChatFunction(tool));
            }

            this.functionExecutor = new FunctionExecutor(functions);
        }

        private static <T> ChatFunction chatTooltoChatFunction(ChatTool<T> tool) {
            return ChatFunction.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .executor(tool.getParameterClass(), tool.getExecutor())
                .build();
        }

        public List<ChatFunction> getFunctions() {
            return this.functionExecutor.getFunctions();
        }

        public ChatMessage call(ChatFunctionCall functionCall) {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode result = objectMapper.valueToTree(functionExecutor.execute(functionCall));

            String json;
            try {
                json = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting json node to json");
            }

            return new ChatMessage(ChatMessageRole.FUNCTION.value(), json, functionCall.getName());
        }

        public static class Builder {
            private List<ChatTool<?>> tools = new ArrayList<ChatTool<?>>();

            public <T> Builder addTool(ChatTool<T> tool) {
                this.tools.add(tool);

                return this;
            }

            public Toolset create() {
                return new Toolset(tools);
            }
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