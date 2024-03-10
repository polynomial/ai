package com.cyster.sage.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.sage.impl.advisors.MumboJumboAdvisor;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;

@Component
public class MumboJumboScenario implements Scenario<Void, Void> {
    private static final String NAME = "mumbo-jumbo";
    private Advisor<Void> advisor;

    MumboJumboScenario(MumboJumboAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Mumbo Jumbo stuff";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        String instructions = "Generate 10 made up words and their definitions";
        
        return this.advisor.createConversation()
            .setOverrideInstructions(instructions)
            .start();
    }

 
}
