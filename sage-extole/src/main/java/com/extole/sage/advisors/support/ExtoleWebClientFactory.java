package com.extole.sage.advisors.support;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.ToolException;

@Component
public class ExtoleWebClientFactory {
    private Optional<String> extoleSuperUserApiKey = Optional.empty();
    private final String extoleBaseUri = "https://api.extole.io/";
        
    ExtoleWebClientFactory(
        @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
        if (extoleSuperUserApiKey != null) {
            this.extoleSuperUserApiKey = Optional.of(extoleSuperUserApiKey);            
        } else {
            System.out.println("Error: extoleSuperUserApiKey not defined or found in environment.EXTOLE_SUPER_USER_API_KEY");            
        }
    }
    
    public WebClient getSuperUserWebClient() throws ToolException {
        if (this.extoleSuperUserApiKey.isEmpty()) {
            throw new FatalToolException("extoleSuperUserApiKey is required");
        }

        return ExtoleWebClientBuilder.builder(extoleBaseUri)
            .setSuperApiKey(this.extoleSuperUserApiKey.get())
            .build();
    }
    
    public WebClient getWebClient(String clientId) throws ToolException {
        if (this.extoleSuperUserApiKey.isEmpty()) {
            throw new FatalToolException("extoleSuperUserApiKey is required");
        }

        if (clientId == null || clientId.isBlank()) {
            throw new FatalToolException("clientId is required");
        }
        if (!clientId.matches("^\\d{1,12}$")) {
            throw new FatalToolException("A clientId is 1 to 12 digits");
        }
        
        return ExtoleWebClientBuilder.builder(extoleBaseUri)
            .setSuperApiKey(this.extoleSuperUserApiKey.get())
            .setClientId(clientId)
            .build();
    }
}
