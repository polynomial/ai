package com.extole.app.jira.ticket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    private TicketCommenter ticketCommenter;

    private static final Logger logger = LogManager.getLogger(TicketController.class);
    private static final Logger eventLogger = LogManager.getLogger("events");

    public TicketController(TicketCommenter ticketCommenter) {
        this.ticketCommenter = ticketCommenter;
    }

    @PostMapping("/ticket")
    public ResponseEntity<Void> ticketEvent(@RequestBody JsonNode request) throws BadRequestException, FatalException {
        eventLogger.info(request.toString());

        if (!request.has("issue_event_type_name") || !request.path("issue_event_type_name").asText().equals(
            "issue_created")) {
            throw new BadRequestException("Unexpected event: " + request.toString());
        }

        var ticketNumber = request.path("issue").path("key").asText();
        logger.info("Ticket - checking: " + ticketNumber);

        if (!ticketNumber.toLowerCase().startsWith("sup")) {
            logger.info("Ticket - " + ticketNumber + " ignored: only processing SUP tickets");
            return ResponseEntity.ok().build();
        }

        if (!request.has("webhookEvent")) {
            logger.info("Ticket - " + ticketNumber + " ignored: no webookEvent attribute");
            return ResponseEntity.ok().build();
        }
            
        if (!request.path("webhookEvent").asText().equalsIgnoreCase("jira:issue_created")) {
             logger.info("Ticket - " + ticketNumber + " ignored: only processing jira:issue_created events");
             return ResponseEntity.ok().build();
        }
        
        ticketCommenter.process(ticketNumber);

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
