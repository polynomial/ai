package com.cyster.sage.impl.scenario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.sage.service.scenario.Scenario;
import com.cyster.sage.service.scenario.ScenarioException;
import com.cyster.sage.service.scenario.ScenarioService;

@Component
public class ScenarioStoreImpl implements ScenarioService {
    private Map<String, Scenario> scenarios = new HashMap<String, Scenario>();

    public ScenarioStoreImpl(List<Scenario> scenarios) {
        for (var scenario : scenarios) {
            this.scenarios.put(scenario.getName(), scenario);
        }
    }

    public Set<Scenario> getScenarios() {
        return scenarios.values().stream().collect(Collectors.toSet());
    }

    public Scenario getScenario(String name) throws ScenarioException {
        if (this.scenarios.containsKey(name)) {
            return this.scenarios.get(name);
        } else {
            throw new ScenarioException("Scenario not found: " + name);
        }
    }
}
