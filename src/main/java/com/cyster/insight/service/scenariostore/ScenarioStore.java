package com.cyster.insight.service.scenariostore;

import com.cyster.insight.service.scenario.Scenario;

public interface ScenarioStore {
	Scenario getScenario(String name) throws ScenarioStoreException;
}
