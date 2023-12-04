package com.cyster.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	Conversation startConversation(Map<String, String> context);
	
}
