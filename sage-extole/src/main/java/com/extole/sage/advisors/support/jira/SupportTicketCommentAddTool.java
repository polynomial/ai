package com.extole.sage.advisors.support.jira;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.adf.AtlassianDocumentMapper;
import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.cyster.sherpa.impl.advisor.Toolset;
import com.extole.sage.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.sage.advisors.support.jira.SupportTicketCommentAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketCommentAddTool implements ExtoleSupportAdvisorTool<Request> {
    private static final Logger logger = LogManager.getLogger(Toolset.class);

    private JiraWebClientFactory jiraWebClientFactory;
    private final boolean testMode;

    SupportTicketCommentAddTool(JiraWebClientFactory jiraWebClientFactory,
        @Value("${JIRA_TEST_MODE:false}") boolean testMode) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.testMode = testMode;
    }

    @Override
    public String getName() {
        return "ticket_comment_add";
    }

    @Override
    public String getDescription() {
        return "Post a comment to a Jira support ticket";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new FatalToolException("Attribute ticket key not specified");
        }

        if (request.comment == null || request.comment.isEmpty()) {
            throw new FatalToolException("Attribute comment must be specified");
        }

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            AtlassianDocumentMapper atlassianDocumentMapper = new AtlassianDocumentMapper();
            payload.set("body", atlassianDocumentMapper.fromMarkdown(request.comment));
        }

        if (testMode) {
            logger.info("Jira (test mode) add comment" + payload.toString());
            return JsonNodeFactory.instance.objectNode().put("status", "success");
        }

        JsonNode result;
        try {
            result = this.jiraWebClientFactory.getWebClient().post()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + request.key + "/comment").build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (Throwable exception) {
            if (exception instanceof WebClientResponseException.BadRequest) {
                throw new FatalToolException(
                    "Invalid format for comment, need to be in Atlassian Document Format (ADF)",
                    exception);
            }
            throw exception;
        }

        if (result == null || !result.has("id")) {
            throw new ToolException("Failed to add comment, unexpected response");
        }

        return result;
    }

    static class Request {
        @JsonPropertyDescription("ticket key")
        @JsonProperty(required = true)
        public String key;

        @JsonPropertyDescription("comment in markdown format")
        @JsonProperty(required = true)
        public String comment;
    }
}
