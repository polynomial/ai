package com.cyster.sage.service.scenariosession;

import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario<?,?> getScenario();
	
	public Conversation getConversation();
	
}
