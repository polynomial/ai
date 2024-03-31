package com.extole.sage.advisors.support;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.extole.sage.advisors.support.reports.ExtoleReportConfiguration;

@Component
public class ExtoleSupportAdvisor implements Advisor<Void> {
    public final String NAME = "extoleSupport";

    private AdvisorService advisorService;
    private List<ExtoleSupportAdvisorTool<?>> tools;
    private Optional<Advisor<Void>> advisor = Optional.empty();

    // Force load of ReportConfiguration
    public ExtoleSupportAdvisor(AdvisorService advisorService, ExtoleReportConfiguration reportConfigurations, List<ExtoleSupportAdvisorTool<?>> tools) {
        this.advisorService = advisorService;
        this.tools = tools;
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

Keep answers brief, and where possible in point form.
When referring to a client, use the client short_name.
""";

            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(instructions);
                
           for(var tool: tools) {
                builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
