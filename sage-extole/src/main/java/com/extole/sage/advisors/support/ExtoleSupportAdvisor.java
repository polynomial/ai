package com.extole.sage.advisors.support;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;

@Component
public class ExtoleSupportAdvisor implements Advisor<Void> {
    public final String NAME = "extole-support";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    private Optional<String> jiraApiKey;
    private Optional<String> jiraBaseUrl;
    private Optional<String> extoleSuperUserApiKey;
    
    public ExtoleSupportAdvisor(AdvisorService advisorService,
        @Value("${jiraApiKey:#{environment.JIRA_API_KEY}}") String jiraApiKey,
        @Value("https://extole.atlassian.net/") String jiraBaseUrl,
        @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
                
        this.advisorService = advisorService;
        this.jiraApiKey = Optional.of(jiraApiKey);
        this.jiraBaseUrl = Optional.of(jiraBaseUrl);
        this.extoleSuperUserApiKey = Optional.of(extoleSuperUserApiKey);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You are an advisor the support team at Extole a SaaS marketing platform.
""";

            AdvisorBuilder<Void>  builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(instructions)
                .withTool(new ExtoleClientSearchTool(this.extoleSuperUserApiKey))
                .withTool(new ExtoleSummaryReportTool(this.extoleSuperUserApiKey))
                .withTool(new SupportTicketSearchTool(this.jiraApiKey, this.jiraBaseUrl))
                .withTool(new SupportTicketGetTool(this.jiraApiKey, this.jiraBaseUrl));
                
            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
