package com.cyster.ai.impl.scenariostore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.service.scenario.Scenario;
import com.cyster.ai.service.scenariostore.ScenarioStore;
import com.cyster.ai.service.scenariostore.ScenarioStoreException;

@Component
public class ScenarioStoreImpl implements ScenarioStore {
    private Map<String, Scenario> scenarios = new HashMap<String, Scenario>();
    
	public ScenarioStoreImpl(List<Scenario> scenarios) {
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
