package com.cyster.ai.app.conversation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.service.conversation.Conversation;
import com.cyster.ai.service.conversation.ConversationException;
import com.cyster.ai.service.conversation.Message;
import com.cyster.ai.service.scenario.Scenario;
import com.cyster.ai.service.scenariosession.ScenarioSessionStore;
import com.cyster.ai.service.scenariostore.ScenarioStore;
import com.cyster.ai.service.scenariostore.ScenarioStoreException;

@RestController
public class ConversationController {
	private ScenarioSessionStore scenarioSessionStore;
	private ScenarioStore scenarioStore;
	        
	public ConversationController(ScenarioSessionStore scenarioSessionStore, ScenarioStore scenarioStore) {
		this.scenarioSessionStore = scenarioSessionStore;
		this.scenarioStore = scenarioStore;
	}

	@GetMapping("/conversations")
	public List<ConversationResponse> index() {
		return scenarioSessionStore.createQueryBuilder().list().stream()
				.map(value -> new ConversationResponse.Builder().setId(value.getId())
						.setMessages(value.getConversation().getMessages()).build())
				.collect(Collectors.toList());
	}

	@PostMapping("/conversations/messages")
	public ConvenienceConversationResponse start_conversation(@RequestBody PromptedConversationRequest request) {		
		if (request == null || request.getScenario().isBlank()) {
			throw new RuntimeException("scenario not specified");
		}
		
		Scenario scenario; 
		try {
		  scenario = this.scenarioStore.getScenario(request.getScenario());
		} catch(ScenarioStoreException exception) {
			throw new RuntimeException("Scenario not found", exception);
		}
		
		Map<String, String> context; 
		if (request.getContext() == null) {
			context = Collections.emptyMap();
		} else {
			context = request.getContext();
		}
		
		Conversation conversation = scenario.startConversation(context);
		if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
		    conversation.addMessage(request.getPrompt());
		}

		var handle = scenarioSessionStore.addSession(scenario, conversation);

		
		Message answer;
		try {
			answer = conversation.respond();
		} catch (ConversationException exception) {
			throw new RuntimeException("Unable to response", exception);
		}

		var response = new ConversationResponse.Builder().setId(handle.getId())
				.setMessages(handle.getConversation().getMessages()).build();

		var conveneinceReponse = new ConvenienceConversationResponse(response, answer.getContent());
		return conveneinceReponse;
	}

}
