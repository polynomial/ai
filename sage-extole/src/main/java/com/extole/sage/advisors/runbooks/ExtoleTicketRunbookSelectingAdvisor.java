package com.extole.sage.advisors.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;

import com.extole.sage.advisors.support.jira.SupportTicketGetTool;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;

@Component
public class ExtoleTicketRunbookSelectingAdvisor implements Advisor<Void> {
    public final String NAME = "extole-ticket-runbook-selector";

    private AdvisorService advisorService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private Optional<Advisor<Void>> advisor = Optional.empty();
    private String defaultRunbookName;
    
    public ExtoleTicketRunbookSelectingAdvisor(AdvisorService advisorService, ExtoleRunbookTool runbookTool, SupportTicketGetTool ticketGetTool,
        ExtoleRunbookOther defaultRunbook) {
        this.advisorService = advisorService;
        this.tools.add(runbookTool);
        this.tools.add(ticketGetTool);
        this.defaultRunbookName = defaultRunbook.getName();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
Given an Extole ticket, you find the best associated Runbook. 
Load the specified ticket.
To build a query string join ticket classification, title and description.
Take the query string
- fix any grammar
- remove duplicate words
- remove PII, URLs, company names "
- remove stop words (common words like \"this\", \"is\", \"in\", \"by\", \"with\" etc), 
- normalize the text (convert to lower case and remove special characters)
- keep to 20 words or less.
 
Use the query string to find the best Runbook. 
Review the runbooks to see which Runbook seems appropriate for the ticket and use its name as the Runbook name. 
If no Runbook is a good match use the Runbook name "%s".
 
Respond just in json in the following form { "ticket_number": "NUMBER", "runbook": "RUNBOOK_NAME" }
""";

            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(String.format(instructions, this.defaultRunbookName));
                
           for(var tool: tools) {
                builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}

