package com.extole.sage.advisors.support;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.sherpa.impl.advisor.CachingTool;
import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.UncachedNotificationGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleNotificationGetTool implements ExtoleSupportAdvisorTool<Request> {
    Tool<Request, Void> tool;

    ExtoleNotificationGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.tool = CachingTool.builder(new UncachedNotificationGetTool(extoleWebClientFactory)).build();
    }

    @Override
    public String getName() {
        return this.tool.getName();
    }

    @Override
    public String getDescription() {
        return this.tool.getDescription();
    }

    @Override
    public Class<Request> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(Request parameters, Void context) throws ToolException {
        return this.tool.execute(parameters, context);
    }
}

class UncachedNotificationGetTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    UncachedNotificationGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
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
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        JsonNode notification = null;

        if (request.userId != null && !request.userId.isBlank()) {
            notification = getClientEventByNotificationIdAndUserId(request);
        }
        if (notification == null) {
            notification = getNotificationById(request);
        }

        return notification;
    }

    private JsonNode getClientEventByNotificationIdAndUserId(Request request)
        throws ToolException {
        JsonNode response;
        try {
            response = this.extoleWebClientFactory.getWebClient(request.clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v6/notifications/" + request.userId)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get user " + request.userId + " notifications");
        }

        if (response == null || !response.isArray()) {
            return null;
        }
        ArrayNode responseArray = (ArrayNode) response;

        JsonNode clientEvent = null;

        for (int i = 0; i < responseArray.size(); i++) {
            JsonNode notification = responseArray.get(i);

            if (notification.has("event_id")
                && notification.path("event_id").asText().equals(request.notificationId)) {

                if (notification.has("client_event")) {
                    clientEvent = notification.path("client_event");
                    break;
                }
            }
        }

        return clientEvent;
    }

    private JsonNode getNotificationById(Request request) throws ToolException {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_id", request.notificationId);
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
            .withClientId(request.clientId)
            .withName("notification_by_event_id")
            .withDisplayName("Notification By Event ID - " + request.notificationId)
            .withParameters(parameters);

        ObjectNode response = reportBuilder.build();
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

    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonProperty(required = false)
        public String userId;

        @JsonProperty(required = true)
        public String notificationId;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Request value = (Request) object;
            return Objects.equals(clientId, value.clientId)
                && Objects.equals(userId, value.userId)
                && Objects.equals(notificationId, value.notificationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, userId, notificationId);
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON",
                    exception);
            }
        }
    }

}
