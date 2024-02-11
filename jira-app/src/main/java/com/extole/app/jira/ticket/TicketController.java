package com.extole.app.jira.ticket;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.extole.sage.scenarios.support.ExtoleSupportTicketScenario;
import com.fasterxml.jackson.databind.JsonNode;

// https://developer.atlassian.com/server/jira/platform/webhooks/

// Grok https://dashboard.ngrok.com/
// Command Line:
//   ngrok http http://localhost:8090
//
// Copy url
//
// Setup webhook:
//   https://extole.atlassian.net/plugins/servlet/webhooks
//   - issue create, comment create

@RestController
public class TicketController {
    private ExtoleSupportTicketScenario supportTicketScenario;

    public TicketController(ExtoleSupportTicketScenario supportTicketScenario) {
        this.supportTicketScenario = supportTicketScenario;
    }

    @PostMapping("/ticket")
    public ResponseEntity<Void> ticketEvent(@RequestBody JsonNode request) throws BadRequestException, FatalException {
        System.out.println("Ticket event: " + request.toPrettyString());

        if (!request.has("issue_event_type_name") || !request.path("issue_event_type_name").asText().equals(
            "issue_created")) {
            throw new BadRequestException("Unexpected event: " + request.toString());
        }

        var ticketNumber = request.path("issue").path("key").asText();
        System.out.println("Processing ticket: " + ticketNumber);

        if (!ticketNumber.toLowerCase().startsWith("sup")) {
            System.out.println("Ticket " + ticketNumber + " ignored - only processing SUP tickets");
            return ResponseEntity.ok().build();
        }

        var context = new HashMap<String, String>();
        {
            context.put("ticket", ticketNumber);
        }

        Message response;
        try {
            response = supportTicketScenario.createConversation().setContext(context).start()
                .addMessage("Please review the new ticket " + ticketNumber)
                .respond();
        } catch (ConversationException exception) {
            exception.printStackTrace();
            throw new FatalException("Problem responding to new ticket: " + ticketNumber, exception);

        }

        System.out.println("Response to new ticket " + ticketNumber + ": " + response);

        return ResponseEntity.ok().build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    static public class BadRequestException extends Exception {
        public BadRequestException(String message) {
            super(message);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    static public class FatalException extends Exception {
        public FatalException(String message) {
            super(message);
        }

        public FatalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
