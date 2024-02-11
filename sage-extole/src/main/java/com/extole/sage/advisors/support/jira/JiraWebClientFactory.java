package com.extole.sage.advisors.support.jira;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.ToolException;

@Component
public class JiraWebClientFactory {
    private Optional<String> jiraApiKey = Optional.empty();
    private final String jiraBaseUri;
        
    JiraWebClientFactory(
        @Value("${jiraApiKey:#{environment.JIRA_API_KEY}}") String jiraApiKey,
        @Value("https://extole.atlassian.net/") String jiraBaseUri) {
        
        if (jiraApiKey != null) {
            this.jiraApiKey = Optional.of(jiraApiKey);            
        } else {
            System.out.println("Error: jiraApiKey not defined or found in environment.EXTOLE_JIRA_API_KEY");            
        }
        
        this.jiraBaseUri = jiraBaseUri;
    }
    
    public WebClient getWebClient() throws ToolException {
        if (this.jiraApiKey.isEmpty()) {
            throw new FatalToolException("jiraApiKey is required");
        }

        return JiraWebClientBuilder.builder(this.jiraBaseUri)
            .setApiKey(this.jiraApiKey.get())
            .build();
    }
    
 
}
