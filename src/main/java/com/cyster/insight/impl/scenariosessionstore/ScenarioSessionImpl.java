package com.cyster.insight.impl.scenariosessionstore;

import com.cyster.insight.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.conversation.Conversation;
import com.cyster.sage.service.scenario.Scenario;

public class ScenarioSessionImpl implements ScenarioSession {
	private String id;
	private Scenario scenario;
	private Conversation conversation;

	ScenarioSessionImpl(String id, Scenario scenario, Conversation conversation) {
		this.id = id;
		this.scenario = scenario;
		this.conversation = conversation;
	}

	public String getId() {
		return this.id;
	}

	public Scenario getScenario() {
		return this.scenario;
	}
	
	public Conversation getConversation() {
		return this.conversation;
	}
}
