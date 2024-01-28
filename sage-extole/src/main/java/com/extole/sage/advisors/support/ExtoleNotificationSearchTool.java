package com.extole.sage.advisors.support;

import java.util.Optional;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleNotificationSearchTool implements Tool<ExtoleNotificationSearchRequest, Void> {
    private Optional<String> extoleSuperUserToken;

    ExtoleNotificationSearchTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
    }

    @Override
    public String getName() {
        return "extole_notification_search";
    }

    @Override
    public String getDescription() {
        return "Finds notifications by notification name";
    }

    @Override
    public Class<ExtoleNotificationSearchRequest> getParameterClass() {
        return ExtoleNotificationSearchRequest.class;
    }

    @Override
    public Object execute(ExtoleNotificationSearchRequest request, Void context) throws ToolException {

        if (this.extoleSuperUserToken.isEmpty()) {
            throw new FatalToolException("extoleSuperUserToken is required");
        }

        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setSuperApiKey(this.extoleSuperUserToken.get())
            .setClientId(request.clientId)
            .build();

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("notification_name", request.notificationName);
        }

        var report = new ExtoleReportBuilder(webClient);
        report.withName("notification_events");
        report.withDisplayName("Notifications - " + request.notificationName);
        report.withParameters(parameters);

        return report.build();
    }

}

class ExtoleNotificationSearchRequest {
    @JsonProperty(required = true)
    public String clientId;

    @JsonPropertyDescription("Query notifications by name (event you have a notification event_id, get the notification to get the name)")
    @JsonProperty(required = false)
    public String notificationName;

}
