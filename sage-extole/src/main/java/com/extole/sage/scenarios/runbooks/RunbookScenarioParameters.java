package com.extole.sage.scenarios.runbooks;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RunbookScenarioParameters {
    private String ticketNumber;
    
    public RunbookScenarioParameters(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    @JsonProperty(required = true)
    public String getTicketNumber() {
        return this.ticketNumber;
    }
}
