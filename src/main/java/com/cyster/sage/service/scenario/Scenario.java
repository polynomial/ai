package com.cyster.sage.service.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.sage.service.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	ConversationBuilder createConversation();
	
	public interface ConversationBuilder {
	    
	    ConversationBuilder setContext(Map<String, String> context);

	    Conversation start();
	}

}
