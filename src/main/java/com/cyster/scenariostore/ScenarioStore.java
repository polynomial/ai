package com.cyster.scenariostore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.scenario.Scenario;

@Component
public class ScenarioStore {
    private Map<String, Scenario> scenarios = new HashMap<String, Scenario>();
    
	public ScenarioStore(List<Scenario> scenarios) {
		for(var scenario: scenarios) {
			this.scenarios.put(scenario.getName(), scenario);
		}
	}
	
	public Scenario getScenario(String name) throws ScenarioStoreException {
		if (this.scenarios.containsKey(name)) {
			return this.scenarios.get(name);
		} else {
		    throw new ScenarioStoreException("Scenario not found: " + name);
		}
	}
}
