package com.extole.sage.advisors.jira;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import org.springframework.http.MediaType;

import com.cyster.sherpa.impl.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Object execute(ExtoleSummaryReportRequest request) {
        
        if (this.extoleSuperUserToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"extoleSuperUserToken is required\" }");
        }

        if (request.clientId == null || request.clientId.isBlank()) {
            return toJsonNode("{ \"error\": \"not client_id specified\" }");            
        }
        
       
        String clientAccessToken = getClientAccessToken(this.extoleSuperUserToken.get(), request.clientId);
        if (clientAccessToken == null || clientAccessToken.isBlank()) {
            return toJsonNode("{ \"error\": \"client_id invalid\" }");            
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
            parameters.put("period", "WEEK");
            
            LocalDate currentDate = LocalDate.now();

            LocalDate endDate = currentDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
            String isoEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
            LocalDate startDate = endDate.minusWeeks(12);
            String isoStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
            var timeRange = isoStartDate + "/" + isoEndDate;
            parameters.put("time_range", timeRange);

            parameters.put("flows", "/business-events");
            // parameters.put("program", "refer-a-friend");
            
            parameters.put("include_totals", "false");
            parameters.put("dimensions", "NONE");            
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
            return toJsonNode("{ \"error\": \"unexpected response\" }");
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
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupt while waiting for report to finish");
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

    private static JsonNode toJsonNode(String json) {
        JsonNode jsonNode;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Unable to parse Json response", exception);
        }
        return jsonNode;
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
    @JsonPropertyDescription("client_id")
    @JsonProperty(required = false)
    public String clientId;

}
