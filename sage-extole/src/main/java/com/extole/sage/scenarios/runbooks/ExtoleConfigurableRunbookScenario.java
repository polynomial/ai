package com.extole.sage.scenarios.runbooks;

import com.cyster.sherpa.service.conversation.Conversation;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

public class ExtoleConfigurableRunbookScenario implements RunbookScenario {
    private String name;
    private String description;
    private String keywords;
    private String instructions;
    private ExtoleSupportAdvisor advisor;

    ExtoleConfigurableRunbookScenario(
        String name, 
        String description, 
        String keywords, 
        String instructions, 
        ExtoleSupportAdvisor advisor) {
        this.name = name;
        this.description = description;
        this.keywords = keywords;
        this.instructions = instructions;
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
        private ExtoleSupportAdvisor advisor;
        
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
        
        public Builder withAdvisor(ExtoleSupportAdvisor advisor) {
            this.advisor = advisor;
            return this;
        }
        
        RunbookScenario build() {
            return new ExtoleConfigurableRunbookScenario(name, description, keywords, instructions, advisor);
        }
    }
}

