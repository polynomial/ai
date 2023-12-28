package com.cyster.insight.service.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.insight.service.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	ConversationBuilder createConversation();
	
	public interface ConversationBuilder {
	    
	    ConversationBuilder setContext(Map<String, String> context);

	    Conversation start();
	}

}
