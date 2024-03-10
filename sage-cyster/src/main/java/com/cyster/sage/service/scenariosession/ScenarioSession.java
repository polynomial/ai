package com.cyster.sage.service.scenariosession;

import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario<?,?> getScenario();
	
	public Conversation getConversation();
	
}
