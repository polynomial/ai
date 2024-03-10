package com.cyster.sage.impl.scenariosessionstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.sage.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.scenariosession.ScenarioSessionStore;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;

@Component
public class SenarioSessioStoreImpl implements ScenarioSessionStore {

    Map<String, ScenarioSession> store;

    SenarioSessioStoreImpl() {
        this.store = new HashMap<String, ScenarioSession>();
    }

    public Optional<ScenarioSession> getSession(String id) {
        if (this.store.containsKey(id)) {
            return Optional.of(this.store.get(id));
        } else {
            return Optional.empty();
        }
    }

    public ScenarioSession addSession(Scenario<?,?> scenario, Conversation conversation) {
        var id = UUID.randomUUID().toString();
        var session = new ScenarioSessionImpl(id, scenario, conversation);

        this.store.put(id, session);

        return session;
    }

    public QueryBuilder createQueryBuilder() {
        return new QueryBuilderImpl(this.store);
    }

    public static class QueryBuilderImpl implements ScenarioSessionStore.QueryBuilder {
        Map<String, ScenarioSession> store;
        int offset = 0;
        int limit = 100;

        QueryBuilderImpl(Map<String, ScenarioSession> store) {
            this.store = store;
        }

        public QueryBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public QueryBuilder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public List<ScenarioSession> list() {
            return this.store.entrySet().stream().skip(this.offset).limit(this.limit).map(Map.Entry::getValue)
                .collect(Collectors.toList());
        }
    }
}