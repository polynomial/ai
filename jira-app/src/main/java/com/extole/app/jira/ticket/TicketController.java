package com.extole.app.jira.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.sage.service.scenariosession.ScenarioSessionStore;
import com.cyster.sherpa.service.scenario.ScenarioService;
import com.fasterxml.jackson.databind.JsonNode;

// https://developer.atlassian.com/server/jira/platform/webhooks/

// Grok https://dashboard.ngrok.com/
// Command Line:
//   ngrok http http://localhost:8080
//
// Copy url
//
// Setup webhook:
//   https://extole.atlassian.net/plugins/servlet/webhooks
//   - create, update
//   - TBD: comment_created

@RestController
public class TicketController {
    private ScenarioSessionStore scenarioSessionStore;
    private ScenarioService scenarioStore;

    public TicketController(ScenarioSessionStore scenarioSessionStore, ScenarioService scenarioStore) {
        this.scenarioSessionStore = scenarioSessionStore;
        this.scenarioStore = scenarioStore;
    }

    @PostMapping("/ticket")
    public ResponseEntity<Void> ticketEvent(@RequestBody JsonNode request) throws BadRequestException {
       System.out.println("ticket event: " + request.toPrettyString());
       
       if (!request.has("issue_event_type_name") || !request.path("issue_event_type_name").asText().equals("issue_created")) {
           throw new BadRequestException("Unexpected event: " + request.toString()); 
       }
       
       var ticketNumber = request.path("issue").path("key").asText();    
       System.out.println("ticketNumber: " + ticketNumber);
          
       return ResponseEntity.ok().build();
    }

    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    static public class BadRequestException extends Exception {
        public BadRequestException(String message) {
            super(message);
        }
    }
    
}
