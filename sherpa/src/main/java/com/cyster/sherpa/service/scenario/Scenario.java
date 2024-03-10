package com.cyster.sherpa.service.scenario;

import com.cyster.sherpa.service.conversation.Conversation;

public interface Scenario<Parameters, Context> {

	String getName();
	
	String getDescription();
	
    Class<Parameters> getParameterClass();
	
	Conversation createConversation(Parameters parameters, Context context);

}
