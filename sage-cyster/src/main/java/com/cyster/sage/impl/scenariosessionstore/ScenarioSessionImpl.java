package com.cyster.sage.impl.scenariosessionstore;

import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.sage.service.scenariosession.ScenarioSession;

public class ScenarioSessionImpl implements ScenarioSession {
	private String id;
	private Scenario<?,?> scenario;
	private Conversation conversation;

	ScenarioSessionImpl(String id, Scenario<?,?> scenario, Conversation conversation) {
		this.id = id;
		this.scenario = scenario;
		this.conversation = conversation;
	}

	public String getId() {
		return this.id;
	}

	public Scenario<?,?> getScenario() {
		return this.scenario;
	}
	
	public Conversation getConversation() {
		return this.conversation;
	}
}
