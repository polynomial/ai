package com.extole.sage.advisors.support;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExtoleReportBuilder {
    WebClient webClient;
    ObjectNode payload = JsonNodeFactory.instance.objectNode();
    String format = "json";
    MediaType mediaType = new MediaType("application", "json");
    Class<?> targetFormat = JsonNode.class;

    ExtoleReportBuilder(WebClient webClient) {
        this.webClient = webClient;
    }

    public ExtoleReportBuilder withName(String name) {
        this.payload.put("name", name);
        return this;
    }

    public ExtoleReportBuilder withDisplayName(String displayName) {
        this.payload.put("display_name", displayName);
        return this;
    }

    public ExtoleReportBuilder withFormat(String format) {
        var formats = this.payload.putArray("formats");
        formats.add(format);
        this.format = format;
        if (format.equals("json")) {
            this.mediaType = new MediaType("applicaton", "json");
            this.targetFormat = JsonNode.class;
        } else if (format.equals("csv")) {
            this.mediaType = new MediaType("text", "csv");
            this.targetFormat = String.class;
        } else {
            throw new RuntimeException("Unsupported download format");
        }

        return this;
    }

    public ExtoleReportBuilder withParameters(ObjectNode parameters) {
        this.payload.set("parameters", parameters);
        return this;

    }

    public Object build() throws ToolException {
        JsonNode report = this.webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (report == null || !report.path("report_id").isEmpty()) {
            throw new ToolException("Internal error, failed to generate report");
        }

        var reportId = report.path("report_id").asText();

        while (!report.path("status").asText().equalsIgnoreCase("DONE")) {
            report = webClient.get()
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

        Object result = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports/" + reportId + "/download" + "." + this.format)
                .build())
            .accept(this.mediaType)
            .retrieve()
            .bodyToMono(this.targetFormat)
            .block();

        return result;
    }
}
