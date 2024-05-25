package com.cyster.ai.weave.service.scenario;

import java.util.List;

public interface ScenarioServiceFactory {
    ScenarioService createScenarioService(List<ScenarioLoader> loader, List<Scenario<?,?>> scenarios);
}
