package com.cyster.insight.app.scenario;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.insight.service.scenariostore.ScenarioStore;

@RestController
public class ScenarioController {

    private ScenarioStore scenarioStore;

    public ScenarioController(ScenarioStore scenarioStore) {
        this.scenarioStore = scenarioStore;
    }

    @GetMapping("/scenarios")
    public Set<ScenarioResponse> index() {
        return scenarioStore.getScenarios().stream().map(scenario -> new ScenarioResponse.Builder()
            .setName(scenario.getName()).setVariables(scenario.variables()).build()).collect(Collectors.toSet());
    }

}
