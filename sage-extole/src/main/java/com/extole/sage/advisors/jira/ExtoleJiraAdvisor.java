package com.extole.sage.advisors.jira;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorService;

@Component
public class ExtoleJiraAdvisor implements Advisor {
    public final String NAME = "extole-support-tickets";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    private Optional<String> jiraApiKey;
    private Optional<String> jiraBaseUrl;
    
    public ExtoleJiraAdvisor(AdvisorService advisorService,
        @Value("${jiraApiKey:#{environment.JIRA_API_KEY}}") String jiraApiKey,
        @Value("https://extole.atlassian.net/") String jiraBaseUrl) {
                
        this.advisorService = advisorService;
        this.jiraApiKey = Optional.of(jiraApiKey);
        this.jiraBaseUrl = Optional.of(jiraBaseUrl);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You help with questions about support tickets at Extole a Marketing SaaS platform.
""";

            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions(instructions)
                .withTool(new SupportTicketSearchTool(this.jiraApiKey, this.jiraBaseUrl))
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
