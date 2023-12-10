package com.cyster.insight.impl.scenariostore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.scenario.Scenario;
import com.cyster.insight.service.scenariostore.ScenarioStore;
import com.cyster.insight.service.scenariostore.ScenarioStoreException;

@Component
public class ScenarioStoreImpl implements ScenarioStore {
    private Map<String, Scenario> scenarios = new HashMap<String, Scenario>();

    public ScenarioStoreImpl(List<Scenario> scenarios) {
        for (var scenario : scenarios) {
            this.scenarios.put(scenario.getName(), scenario);
        }
    }

    public Set<Scenario> getScenarios() {
        return scenarios.values().stream().collect(Collectors.toSet());
    }

    public Scenario getScenario(String name) throws ScenarioStoreException {
        if (this.scenarios.containsKey(name)) {
            return this.scenarios.get(name);
        } else {
            throw new ScenarioStoreException("Scenario not found: " + name);
        }
    }
}
