package com.cyster.ai.service.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.ai.service.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	Conversation startConversation(Map<String, String> context);
	
}
