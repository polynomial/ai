package com.cyster.insight.service.scenariostore;

import java.util.Set;

import com.cyster.insight.service.scenario.Scenario;

public interface ScenarioStore {

    Set<Scenario> getScenarios();

    Scenario getScenario(String name) throws ScenarioStoreException;
}
