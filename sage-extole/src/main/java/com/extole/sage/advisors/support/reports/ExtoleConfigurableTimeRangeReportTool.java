package com.extole.sage.advisors.support.reports;

import java.util.Map;
import java.util.Objects;

import com.cyster.sherpa.impl.advisor.CachingTool;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.sage.advisors.support.ExtoleWebClientFactory;
import com.extole.sage.advisors.support.reports.UncachedExtoleConfigurableTimeRangeReportTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleConfigurableTimeRangeReportTool implements ExtoleSupportAdvisorTool<Request> {
    Tool<Request, Void> tool;
    
    ExtoleConfigurableTimeRangeReportTool(String name, String reportName, int rowLimit, Map<String, Object> fixedParameters, ExtoleWebClientFactory extoleWebClientFactory) {
        this.tool = CachingTool.builder(new UncachedExtoleConfigurableTimeRangeReportTool(name, reportName, rowLimit, fixedParameters, extoleWebClientFactory)).build();
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

    public static class Builder {
        private String name;
        private String reportName;
        private int rowLimit = 10;
        private Map<String, Object> parameters;
        private ExtoleWebClientFactory extoleWebClientFactory;
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withReportName(String reportName) {
            this.reportName = reportName;
            return this;
        }

        public Builder withRowLimit(int rowLimit) {
            this.rowLimit = rowLimit;
            return this;
        }
        

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder withExtoleWebClientFactory(ExtoleWebClientFactory extoleWebClientFactory) {
            this.extoleWebClientFactory = extoleWebClientFactory;
            return this;
        }
        
        public ExtoleConfigurableTimeRangeReportTool build() {
            return new ExtoleConfigurableTimeRangeReportTool(name, reportName, rowLimit, parameters, extoleWebClientFactory);
        }
    }
}


class UncachedExtoleConfigurableTimeRangeReportTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;
    private String name;
    private String reportName;
    private int rowLimit;
    private Map<String, Object> fixedParameters;
    
    UncachedExtoleConfigurableTimeRangeReportTool(String name, String reportName, int rowLimit, Map<String, Object> fixedParameters, 
        ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;        
        this.name = name;
        this.reportName = reportName;
        this.rowLimit = rowLimit;
        this.fixedParameters = fixedParameters;
    }

    @Override
    public String getName() {
        return "extole_top_promotion_sources";
    }

    @Override
    public String getDescription() {
        return "Runs a report giving the traffic to the top promotion sources";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        { 
            var timeRange = "LAST_MONTH";
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            parameters.put("time_range", timeRange);
            
            ObjectMapper mapper = new ObjectMapper();
            fixedParameters.forEach((key, value) -> {
                JsonNode jsonNode = mapper.valueToTree(value);
                parameters.set(key, jsonNode);
            });

        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
                .withClientId(request.clientId)
                .withLimit(rowLimit) 
                .withName(reportName)
                .withDisplayName(name)
                .withParameters(parameters);
        
        return reportBuilder.build();
    }
    
    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 4 weeks")
        @JsonProperty(required = false)
        public String timeRange;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) { 
                return false;
            }
            
            Request value = (Request) object;
            return Objects.equals(clientId, value.clientId) &&
                   Objects.equals(timeRange, value.timeRange);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, timeRange);
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
}

