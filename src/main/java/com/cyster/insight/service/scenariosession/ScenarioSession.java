package com.cyster.insight.service.scenariosession;

import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario getScenario();
	
	public Conversation getConversation();
	
}
