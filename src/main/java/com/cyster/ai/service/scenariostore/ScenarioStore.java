package com.cyster.ai.service.scenariostore;

import com.cyster.ai.service.scenario.Scenario;

public interface ScenarioStore {
	Scenario getScenario(String name) throws ScenarioStoreException;
}
