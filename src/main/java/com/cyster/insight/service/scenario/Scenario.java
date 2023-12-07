package com.cyster.insight.service.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.insight.service.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	Conversation startConversation(Map<String, String> context);
	
}
