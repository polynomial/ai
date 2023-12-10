package com.cyster.insight.impl.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.Message;
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
    private List<ChatFunction> tools;

    public TooledChatConversation(OpenAiFactoryImpl openAiFactory) {
        this.openAiFactory = openAiFactory;
        this.messages = new ArrayList<Message>();
        this.tools = new ArrayList<ChatFunction>();
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
        ChatFunction tool = ChatFunction.builder().name(name).description(description)
            .executor(parameterClass, executor).build();
        this.tools.add(tool);
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

            FunctionExecutor functionExecutor = new FunctionExecutor(this.tools);

            var chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(chatMessages)
                .functions(functionExecutor.getFunctions())
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
                messages.add(new Message(Message.Type.AI, functionCall.getName() + "(" + functionCall.getArguments() + ")"));

                ChatMessage functionResponseMessage = functionExecutor.executeAndConvertToMessageHandlingExceptions(
                    functionCall);
                messages.add(new Message(Message.Type.FUNCTION, functionResponseMessage.getContent()));
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
            // .filter(message -> message.getType() == Message.Type.AI || message.getType()
            // == Message.Type.USER)
            .collect(Collectors.toList());
    }

}