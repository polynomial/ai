package com.extole.sage.advisors.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import org.springframework.http.MediaType;
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

import com.extole.sage.advisors.support.UncachedExtoleTopPromptionSourcesReportTool.Request;

@Component
class ExtoleTopPromptionSourcesReportTool implements ExtoleSupportAdvisorTool<Request> {
    Tool<Request, Void> tool;
    
    ExtoleTopPromptionSourcesReportTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.tool = CachingTool.builder(new UncachedExtoleTopPromptionSourcesReportTool(extoleWebClientFactory)).build();
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


class UncachedExtoleTopPromptionSourcesReportTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    UncachedExtoleTopPromptionSourcesReportTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
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
        
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.put("name", "TOP_PROMOTION_SOURCES_V2");

            var now = LocalDateTime.now();
            var stamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            payload.put("display_name", "Top Promotion Sources - " + stamp + " AI");

            var format = payload.putArray("formats");
            format.add("CSV");
            var scopes = payload.putArray("scopes");
            scopes.add("CLIENT_SUPERUSER");

            var parameters = payload.putObject("parameters");

            parameters.put("container", "production");
            parameters.put("top_sources_count", "10");

            var period = "DAY";
            if (request.period != null && !request.period.isBlank()) {
                period = request.period;
            }
            parameters.put("period", period);

            //var timeRange = "LAST_MONTH";
            LocalDate currentDate = LocalDate.now();

            LocalDate endDate = currentDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
            String isoEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
            LocalDate startDate = endDate.minusWeeks(4);
            String isoStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
            var timeRange = isoStartDate + "/" + isoEndDate;

            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            parameters.put("time_range", timeRange);

            parameters.put("visit_type", "ALL");
            parameters.put("steps", "promotion_viewed");
            parameters.put("sort_order", "descending(promotion_viewed)");
            parameters.put("quality_all", "ALL");

        }

        var webClient = this.extoleWebClientFactory.getWebClient(request.clientId);
        
        var reportNode = webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (!reportNode.path("report_id").isEmpty()) {
            throw new ToolException("Internal error, failed to generate report");
        }
        var reportId = reportNode.path("report_id").asText();

        while (!reportNode.path("status").asText().equalsIgnoreCase("DONE")) {
            reportNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + reportId)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                throw new RuntimeException("Interrupt while waiting for report to finish", exception);
            }
        }

        var result = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports/" + reportId + "/download.csv")
                .build())
            .accept(new MediaType("text", "csv"))
            .retrieve()
            .bodyToMono(String.class)
            .block();

        return result;
    }
    
    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 4 weeks")
        @JsonProperty(required = false)
        public String timeRange;

        @JsonPropertyDescription("period for each row in the report, defaults to DAY, Support periods include: HOUR, DAY, WEEK")
        @JsonProperty(required = false)
        public String period;
        
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
                   Objects.equals(timeRange, value.timeRange) &&
                   Objects.equals(period, value.period);

        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, timeRange, period);
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

