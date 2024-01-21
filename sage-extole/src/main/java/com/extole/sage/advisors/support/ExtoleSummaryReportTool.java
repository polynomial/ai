package com.extole.sage.advisors.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleSummaryReportTool implements Tool<ExtoleSummaryReportRequest> {
    private Optional<String> extoleSuperUserToken;
    
    ExtoleSummaryReportTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
    }

    @Override
    public String getName() {
        return "extole_summary_report";
    }

    @Override
    public String getDescription() {
        return "Runs a report to count events";
    }

    @Override
    public Class<ExtoleSummaryReportRequest> getParameterClass() {
        return ExtoleSummaryReportRequest.class;
    }

    @Override
    public Object execute(ExtoleSummaryReportRequest request) throws ToolException {
        
        if (this.extoleSuperUserToken.isEmpty()) {
            throw new FatalToolException("extoleSuperUserToken is required");
        }

        if (request.clientId == null || request.clientId.isBlank()) {
            return new ToolException("Attributed client_id not specified\" }");            
        }
        
       
        String clientAccessToken = getClientAccessToken(this.extoleSuperUserToken.get(), request.clientId);
        if (clientAccessToken == null || clientAccessToken.isBlank()) {
            return new ToolException("Attribute client_id is invalid: " + request.clientId);            
        }
       
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(clientAccessToken)
            .build();

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {

            payload.put("name", "summary");
            
            var now = LocalDateTime.now();
            var stamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            payload.put("display_name", "Summary - simple " + stamp + " AI");
            
            var format = payload.putArray("formats");
            format.add("CSV");
            var scopes = payload.putArray("scopes");
            scopes.add("CLIENT_SUPERUSER");
            
            var parameters = payload.putObject("parameters");
            
            parameters.put("container", "production");
            
            var period = "WEEK";
            if (request.period != null && !request.period.isBlank()) {
                period = request.period;
            }
            parameters.put("period", period);
            
            LocalDate currentDate = LocalDate.now();

            LocalDate endDate = currentDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
            String isoEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
            LocalDate startDate = endDate.minusWeeks(12);
            String isoStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
            var timeRange = isoStartDate + "/" + isoEndDate;
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            
            parameters.put("time_range", timeRange);

            parameters.put("flows", "/business-events");
            
            parameters.put("include_totals", "false");
            
            var dimensions = "NONE";
            if (request.dimensions != null && !request.dimensions.isEmpty()) {
                for(var dimension: request.dimensions) {
                    switch(dimension) {
                    case "SOURCE":
                        // Work around bug, can't ask for SOURCE by itself
                        dimension = "SOURCE_TYPE,SOURCE";
                        break;
                    }
                    dimensions = String.join(",", request.dimensions);
                }
            }
            parameters.put("dimensions", dimensions);            
        }        
        
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
        
        while(!reportNode.path("status").asText().equalsIgnoreCase("DONE")) {
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
    
    private static String getClientAccessToken(String superUserAccessToken, String clientId) {
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(superUserAccessToken)
            .build();

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.put("client_id", clientId);
        }

        var resultNode = webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/tokens")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
        
        return resultNode.path("access_token").asText();
    }
}

class ExtoleSummaryReportRequest {
    @JsonProperty(required = true)
    public String clientId;
    
    @JsonPropertyDescription("dimensions by which to segment the summary data, defaults to no dimensions. Supported dimensions are: PROGRAM, CAMPAIGN, SOURCE, CHANNEL, VARIANT, VISIT_TYPE and QUALITY")
    @JsonProperty(required = false)
    public List<String> dimensions;

    @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 12 weeks")
    @JsonProperty(required = false)
    public String timeRange;

    @JsonPropertyDescription("period for each row in the report, defaults to WEEK, Support periods include: HOUR, DAY, WEEK")
    @JsonProperty(required = false)
    public String period;
}
