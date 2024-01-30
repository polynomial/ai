package com.extole.sage.advisors.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleNotificationGetTool implements ExtoleSupportAdvisorTool<ExtoleNotificationGetRequest> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleNotificationGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_notification_get";
    }

    @Override
    public String getDescription() {
        return "Retrieve a notification by notification_id";
    }

    @Override
    public Class<ExtoleNotificationGetRequest> getParameterClass() {
        return ExtoleNotificationGetRequest.class;
    }

    @Override
    public Object execute(ExtoleNotificationGetRequest request, Void context) throws ToolException {

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_id", request.notificationId);
        }

        var report = new ExtoleReportBuilder(this.extoleWebClientFactory.getWebClient(request.clientId));
        report.withName("notification_by_event_id");
        report.withDisplayName("Notification By Event ID - " + request.notificationId);
        report.withParameters(parameters);

        ObjectNode response = report.build();
        if (response == null || response.path("data").isEmpty()) {
            throw new ToolException("Problem searching for notification");
        }

        if (response.path("data").isEmpty() || !response.path("data").isArray()) {
            throw new ToolException("Problem loading notification");
        }

        ArrayNode data = (ArrayNode) response.path("data");
        if (data.size() != 1) {
            throw new ToolException("Notification not found");
        }

        JsonNode notification = data.get(0);
        if (!notification.isObject()) {
            throw new ToolException("Notification invalid");
        }
        ObjectNode notificationNode = (ObjectNode) notification;

        notificationNode.put("notification_id", notificationNode.get("event_id").asText());
        notificationNode.remove("event_id");

        return notificationNode;
    }
}

class ExtoleNotificationGetRequest {
    @JsonProperty(required = true)
    public String clientId;

    @JsonProperty(required = true)
    public String notificationId;
}
