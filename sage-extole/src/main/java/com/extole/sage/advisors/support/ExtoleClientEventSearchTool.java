package com.extole.sage.advisors.support;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.CachingTool;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleClientEventSearchTool implements ExtoleSupportAdvisorTool<ExtoleClientEventSearchRequest> {
    Tool<ExtoleClientEventSearchRequest, Void> tool;
    
    ExtoleClientEventSearchTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.tool = CachingTool.builder(new UncachedClientEventSearchTool(extoleWebClientFactory)).build();
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
    public Class<ExtoleClientEventSearchRequest> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(ExtoleClientEventSearchRequest parameters, Void context) throws ToolException {
        return this.tool.execute(parameters, context);
    }   
}

class UncachedClientEventSearchTool implements ExtoleSupportAdvisorTool<ExtoleClientEventSearchRequest> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    UncachedClientEventSearchTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
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
    public Class<ExtoleClientEventSearchRequest> getParameterClass() {
        return ExtoleClientEventSearchRequest.class;
    }

    @Override
    public Object execute(ExtoleClientEventSearchRequest request, Void context) throws ToolException {

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_name", request.eventName);
        }

        var report = new ExtoleReportBuilder(this.extoleWebClientFactory.getWebClient(request.clientId));
        report.withName("client_events");
        report.withDisplayName("Client Events - " + request.eventName);
        report.withParameters(parameters);

        return report.build();
    }

}

class ExtoleClientEventSearchRequest {
    @JsonProperty(required = true)
    public String clientId;

    @JsonPropertyDescription("Query client events by event name")
    @JsonProperty(required = false)
    public String eventName;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) { 
            return false;
        }
        
        ExtoleClientEventSearchRequest value = (ExtoleClientEventSearchRequest) object;
        return Objects.equals(clientId, value.clientId) &&
               Objects.equals(eventName, value.eventName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, eventName);
    }   
    
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON", exception);
        }
    }
}
