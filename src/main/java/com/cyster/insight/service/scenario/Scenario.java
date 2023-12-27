package com.cyster.insight.service.scenario;

import java.util.Map;
import java.util.Set;

import com.cyster.insight.service.conversation.Conversation;

public interface Scenario {

	String getName();
	
	Set<String> variables();
	
	// TODO change to createAssistant - assistant has conversation
	ConversationBuilder createConversation();
	
	public interface ConversationBuilder {
	    
	    ConversationBuilder setContext(Map<String, String> context);
	    
	    // TOOD remove
	    ConversationBuilder setAccessToken(String token);
	    
	    Conversation start();
	}

}
