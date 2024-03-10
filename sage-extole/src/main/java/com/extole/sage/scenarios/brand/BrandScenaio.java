package com.extole.sage.scenarios.brand;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.brand.ExtoleBrandAdvisor;

@Component
public class BrandScenaio implements Scenario<Void, Void> {
    public static String NAME = "extole_brand";
    private Advisor<Void> advisor;

    BrandScenaio(ExtoleBrandAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Provides details about a Brand";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        return this.advisor.createConversation().start();
    }

}
