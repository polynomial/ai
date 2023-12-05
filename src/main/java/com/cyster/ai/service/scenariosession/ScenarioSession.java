package com.cyster.ai.service.scenariosession;

import com.cyster.ai.service.conversation.Conversation;
import com.cyster.ai.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario getScenario();
	
	public Conversation getConversation();
	
}
