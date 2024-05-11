package com.extole.sage.scenarios.runbooks;

import com.cyster.assistant.service.scenario.Scenario;

public interface RunbookScenario extends Scenario<RunbookScenarioParameters, Void> {
    String getName();
    String getKeywords();
}
