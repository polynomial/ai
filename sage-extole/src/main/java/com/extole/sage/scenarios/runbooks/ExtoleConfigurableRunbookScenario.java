package com.extole.sage.scenarios.runbooks;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;

public class ExtoleConfigurableRunbookScenario implements RunbookScenario {
    private String name;
    private String description;
    private String keywords;
    private String instructions;
    private Advisor<Void> advisor;

    private ExtoleConfigurableRunbookScenario(String name, String description, String keywords, String instructions, Advisor<Void> advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public String getKeywords() {
        return this.keywords;
    }
    
    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }
 
    @Override
    public Conversation createConversation(RunbookScenarioParameters parameters, Void context) {
        return this.advisor.createConversation().setOverrideInstructions(this.instructions).start();
    }
    
    public static class Builder {
        private String name;
        private String description;
        private String keywords;
        private String instructions;
        private Advisor<Void> advisor;
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder withKeywords(String keywords) {
            this.keywords = keywords;
            return this;
        }
    
        public Builder withInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }        
        
        public Builder withAdvisor(Advisor<Void> advisor) {
            this.advisor = advisor;
            return this;
        }
        
        RunbookScenario build() {
            return new ExtoleConfigurableRunbookScenario(name, description, keywords, instructions, advisor);
        }
    }
}

