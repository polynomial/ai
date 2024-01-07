package com.cyster.sage.service.scenariosession;

import java.util.List;
import java.util.Optional;

import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;

public interface ScenarioSessionStore {

    Optional<ScenarioSession> getSession(String id);

    ScenarioSession addSession(Scenario scenario, Conversation conversation);

    QueryBuilder createQueryBuilder();

    public interface QueryBuilder {

        public QueryBuilder setOffset(int offset);

        public QueryBuilder setLimit(int limit);

        public List<ScenarioSession> list();
    }
}
