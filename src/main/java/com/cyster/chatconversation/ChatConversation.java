package com.cyster.chatconversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cyster.conversation.Conversation;
import com.cyster.conversation.Message;
import com.cyster.openai.OpenAiFactory;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

public class ChatConversation implements Conversation {

	private final String model = "gpt-3.5-turbo";

	private OpenAiFactory openAiFactory;
	private String scenarioName;
	private Map<String, String> context;
	private List<Message> messages;

	public ChatConversation(OpenAiFactory openAiFactory, String scenarioName, Map<String, String> context) {
		this.openAiFactory = openAiFactory;
		this.scenarioName = scenarioName;
		this.context = Collections.unmodifiableMap(context);
		this.messages = new ArrayList<Message>();
	}

	@Override
	public void addMessage(String content) {
		this.messages.add(new Message(content));
	}

	public void addSystemMessage(String content) {
		this.messages.add(new Message(Message.Type.SYSTEM, content));
	}

	public void addAiMessage(String content) {
		this.messages.add(new Message(Message.Type.AI, content));
	}

	@Override
	public Message respond() {
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

		var chatCompletionRequest = ChatCompletionRequest.builder().model(model).messages(chatMessages).build();

		var result = this.openAiFactory.getService().createChatCompletion(chatCompletionRequest);

		var choices = result.getChoices();
		if (choices.size() > 1) {
			messages.add(new Message(Message.Type.INFO, "Multiple responses (ignored)"));
		}
		var message = new Message(Message.Type.AI, choices.get(0).getMessage().getContent());
		messages.add(message);
		return message;
	}

	@Override
	public List<Message> getMessages() {
		return this.messages;
	}

	@Override
	public String getScenarioName() {
		return this.scenarioName;
	}

	@Override
	public Map<String, String> getContext() {
		return this.context;
	}

}