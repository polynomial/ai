package com.extole.sage.advisors.support;

import java.util.Optional;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleNotificationGetTool implements Tool<ExtoleNotificationGetRequest, Void> {
    private Optional<String> extoleSuperUserToken;

    ExtoleNotificationGetTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
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

        if (this.extoleSuperUserToken.isEmpty()) {
            throw new FatalToolException("extoleSuperUserToken is required");
        }

        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setSuperApiKey(this.extoleSuperUserToken.get())
            .setClientId(request.clientId)
            .build();

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_id", request.notificationId);
        }

        var report = new ExtoleReportBuilder(webClient);
        report.withName("notification_by_event_id");
        report.withDisplayName("Notification By Event ID - " + request.notificationId);
        report.withParameters(parameters);

        ArrayNode response = (ArrayNode) report.build(); // TODO fix need to cast
        if (response == null || response.size() == 0) {
            return response;
        }

        ArrayNode modifiedResponse = JsonNodeFactory.instance.arrayNode();
        for (JsonNode row : response) {
            ObjectNode modifiedRow = JsonNodeFactory.instance.objectNode();

            modifiedRow.put("notification_id", row.path("event_id").asText());
            row.fields().forEachRemaining(field -> {
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (!"event_id".equals(fieldName)) {
                    modifiedRow.set(fieldName, fieldValue);
                }
            });
            modifiedResponse.add(modifiedRow);
        }

        return modifiedResponse;
    }
}

class ExtoleNotificationGetRequest {
    @JsonProperty(required = true)
    public String clientId;

    @JsonProperty(required = true)
    public String notificationId;
}
