package com.cyster.scenarioai.conversation;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.chatconversation.ChatConversation;
import com.cyster.chatconversation.ChatConversationFactory;
import com.cyster.conversationstore.ConversationStore;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@RestController
public class ConversationController {
	private ConversationStore store;
	private ChatConversationFactory chatConversationFactory;

	public ConversationController(ConversationStore store, ChatConversationFactory chatConversationFactory) {
		this.store = store;
		this.chatConversationFactory = chatConversationFactory;
	}

	@GetMapping("/conversations")
	public List<ConversationResponse> index() {
		return store.createQueryBuilder().list().stream()
				.map(value -> new ConversationResponse.Builder().setId(value.getId())
						.setScenario(value.getConversation().getScenarioName())
						.setMessages(value.getConversation().getMessages()).build())
				.collect(Collectors.toList());
	}

	@PostMapping("/conversations/messages")
	public ConvenienceConversationResponse start_conversation(@RequestBody PromptedConversationRequest request) {
		ChatConversation conversation = chatConversationFactory.newConversation(request.getScenario(),
				request.getContext());

		String systemPrompt = "Please translate this message from {{language}} to {{target_language}}";

		MustacheFactory mostacheFactory = new DefaultMustacheFactory();
		Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
		var messageWriter = new StringWriter();
		mustache.execute(messageWriter, request.getContext());
		messageWriter.flush();

		conversation.addSystemMessage(messageWriter.toString());
		conversation.addMessage(request.getPrompt());

		var handle = store.addConverstation(conversation);

		var answer = conversation.respond();

		var response = new ConversationResponse.Builder().setId(handle.getId())
				.setScenario(handle.getConversation().getScenarioName())
				.setMessages(handle.getConversation().getMessages()).build();

		var conveneinceReponse = new ConvenienceConversationResponse(response, answer.getContent());
		return conveneinceReponse;
	}

}
