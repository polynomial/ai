package com.cyster.app.sage.scenario;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.sherpa.service.scenario.ScenarioService;

@RestController
public class ScenarioController {

    private ScenarioService scenarioStore;

    public ScenarioController(ScenarioService scenarioStore) {
        this.scenarioStore = scenarioStore;
    }

    @GetMapping("/scenarios")
    public Set<ScenarioResponse> index() {
        return scenarioStore.getScenarios().stream().map(scenario -> new ScenarioResponse.Builder()
            .setName(scenario.getName())
            .setDescription(scenario.getDescription())
            //.setVariables(null)
            .build()).collect(Collectors.toSet());
    }

}
