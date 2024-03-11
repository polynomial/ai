package com.extole.sage.advisors.support;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.CachingTool;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.extole.sage.advisors.support.ExtoleClientEventSearchTool.Request;

@Component
class ExtoleClientEventSearchTool implements ExtoleSupportAdvisorTool<Request> {
    private Tool<Request, Void> tool;
    
    ExtoleClientEventSearchTool(ExtoleWebClientFactory extoleWebClientFactory, ExtoleNotificationGetTool extoleNotificationGetTool) {
        this.tool = CachingTool.builder(new UncachedClientEventSearchTool(extoleWebClientFactory, extoleNotificationGetTool)).build();
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
    
    static class Request {
        @JsonProperty(required = true)
        public String client_id;

        @JsonPropertyDescription("Query for client events like the client event that triggered the notification with this id")
        @JsonProperty(required = false)
        public String like_notifcation_id;


        @JsonPropertyDescription("Query for client events caused by user_id")
        @JsonProperty(required = false)
        public String user_id;
        
        
        @JsonPropertyDescription("Query client events by tags, a comma seperated list of tags.")
        @JsonProperty(required = false)
        public String tags;

        @JsonPropertyDescription("Query client events by event_name")
        @JsonProperty(required = false)
        public String event_name;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Request value = (Request) object;
            return Objects.equals(client_id, value.client_id)
                && Objects.equals(user_id, value.user_id)
                && Objects.equals(event_name, value.event_name)
                && Objects.equals(tags, value.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(client_id, tags);
//            return Objects.hash(clientId, eventName, tags);

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

class UncachedClientEventSearchTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;
    private ExtoleNotificationGetTool extoleNotificationGetTool;

    UncachedClientEventSearchTool(ExtoleWebClientFactory extoleWebClientFactory, ExtoleNotificationGetTool extoleNotificationGetTool) {
        this.extoleWebClientFactory = extoleWebClientFactory;
        this.extoleNotificationGetTool = extoleNotificationGetTool;
    }

    @Override
    public String getName() {
        return "extole_client_event_search";
    }

    @Override
    public String getDescription() {
        return "Finds client events by event name or user";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        var tags = "";
        if (request.tags != null) {
            tags = request.tags;
        }
        
        if (request.like_notifcation_id != null) {
            var notificationRequest = new com.extole.sage.advisors.support. ExtoleNotificationGetTool.Request();
            notificationRequest.clientId = request.client_id;
            notificationRequest.userId = request.user_id;
            notificationRequest.notificationId = request.like_notifcation_id;
            
            JsonNode notification = (JsonNode)this.extoleNotificationGetTool.execute(notificationRequest, null);
            
            JsonNode tagsNode = new ObjectMapper().createArrayNode();
            if (notification.has("tags")) {
                tagsNode = notification.path("tags");
            }
            else if (notification.has("client_event") && notification.path("client_event").has("tags")) {
                tagsNode = notification.path("client_event").path("tags");
            }
            
            if (tagsNode.isArray()) {
                for (JsonNode tagNode : tagsNode) {
                    if (tags.length() > 0 && !tags.toString().trim().isEmpty()) {
                        tags = tags + ",";
                    }
                    tags = tags + tagNode.asText();
                }
            }
        }
        
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("time_range", "LAST_QUARTER");
            if (request.user_id != null) {
                parameters.put("event_user", request.user_id);
            }
            if (request.event_name != null) {
                parameters.put("event_name", request.event_name);
            }
            if (!tags.isBlank()) {
                parameters.put("matching_all_tags", tags);
            }
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
            .withClientId(request.client_id)
            .withLimit(2) 
            .withName("client_events")
            .withDisplayName("Client Events - tags:" +  tags)
            .withParameters(parameters);

        return reportBuilder.build();
    }


}


