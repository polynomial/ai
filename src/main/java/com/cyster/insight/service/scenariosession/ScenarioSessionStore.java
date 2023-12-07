package com.cyster.insight.service.scenariosession;

import java.util.List;

import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;

public interface ScenarioSessionStore {

	ScenarioSession getSession(String id);

	ScenarioSession addSession(Scenario scenario, Conversation conversation);

	QueryBuilder createQueryBuilder();

	public interface QueryBuilder {

		public QueryBuilder setOffset(int offset);
		
		public QueryBuilder setLimit(int limit);

		public List<ScenarioSession> list();
	}
}
