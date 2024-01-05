package com.cyster.sage.service.scenario;

import java.util.Set;

public interface ScenarioService {

    Set<Scenario> getScenarios();

    Scenario getScenario(String name) throws ScenarioException;
}
