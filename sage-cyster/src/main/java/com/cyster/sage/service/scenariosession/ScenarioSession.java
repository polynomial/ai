package com.cyster.sage.service.scenariosession;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario<?,?> getScenario();
	
	public Conversation getConversation();
	
}
