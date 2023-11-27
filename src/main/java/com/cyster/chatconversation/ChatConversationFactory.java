package com.cyster.chatconversation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ai.openai.llm.OpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyster.conversation.Conversation;
import com.cyster.conversation.ConversationFactory;
import com.cyster.conversation.Message;
import com.theokanning.openai.service.OpenAiService;

import org.springframework.ai.core.llm.LlmClient;

class ChatConversation implements Conversation {

	private final String model = "gpt-3.5-turbo";
	
	private LlmClient llmClient;
	private List<Message> messages;
	
	public ChatConversation(OpenAiService openAiService) {
	  OpenAiClient openAiClient = new OpenAiClient(openAiService);
	  openAiClient.setModel(model);
	  
	  this.llmClient = openAiClient;
	  this.messages = Collections.emptyList();
	}
	
	@Override
	public void addMessage(String content) {
		this.messages.add(new Message(content));
		
	}

	@Override
	public Message respond() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> messages() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

public class ChatConversationFactory implements ConversationFactory {

	private OpenAiService openAiService;
	
    @Autowired
    public ChatConversationFactory(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }
    
	@Override
	public String getName() {
		return "chat";
	}

	@Override
	public Conversation newConversation(Map<String, String> context) {
		return new ChatConversation(openAiService);
	}
	
}


