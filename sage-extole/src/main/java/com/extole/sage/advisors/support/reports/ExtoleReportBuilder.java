package com.extole.sage.advisors.support.reports;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.ExtoleWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


// TODO
// default to json and csv
// scopes SUPER_USER

public class ExtoleReportBuilder {
    ExtoleWebClientFactory webClientFactory;
    Optional<String> clientId = Optional.empty();
    ObjectNode payload = JsonNodeFactory.instance.objectNode();
    String format = "json";
    MediaType mediaType = new MediaType("application", "json");
    Class<?> targetFormat = JsonNode.class;
    private int offset = 0;
    private int limit = 10;

    ExtoleReportBuilder(ExtoleWebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    public ExtoleReportBuilder withClientId(String clientId) {
        this.clientId = Optional.of(clientId);
        return this;
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

    public ExtoleReportBuilder withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public ExtoleReportBuilder withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public ObjectNode build() throws ToolException {
        WebClient webClient;
        if (this.clientId.isPresent()) {
            webClient = this.webClientFactory.getWebClient(this.clientId.get());
        } else {
            webClient = this.webClientFactory.getSuperUserWebClient();

        }

        JsonNode report = webClient.post()
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

        var info = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports/" + reportId + "/info")
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        ObjectNode enrichedResult = JsonNodeFactory.instance.objectNode();
        enrichedResult.put("report_id", report.path("report_id").asText());
        enrichedResult.put("download_uri", report.path("download_uri").asText());
        enrichedResult.put("view_uri", "https://my.extole.com/reports/view?client_id=" + this.clientId.get() + "#"
            + report.path("report_id").asText());

        enrichedResult.put("total_rows", info.path("total_rows").asInt());

        ObjectNode page = enrichedResult.putObject("page");
        page.put("row_start", this.offset);

        if (this.format.equalsIgnoreCase("json")) {
            ArrayNode result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + reportId + "/download" + "." + this.format)
                    .queryParam("offset", this.offset)
                    .queryParam("limit", this.limit)
                    .build())
                .accept(this.mediaType)
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .block();

            page.put("row_count", result.size());
            enrichedResult.putArray("data").addAll(result);
        } else if (this.format.equalsIgnoreCase("csv")) {
            String csv = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + reportId + "/download" + "." + this.format)
                    .queryParam("offset", this.offset)
                    .queryParam("limit", this.limit)
                    .build())
                .accept(this.mediaType)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            page.put("row_count", csv.split("\r?\n").length);
            enrichedResult.put("data", csv);
        } else {
            throw new RuntimeException("format not supported");
        }

        return enrichedResult;
    }
}
