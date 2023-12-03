package com.cyster.conversationstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.conversation.Conversation;

@Component
public class ConversationStore {

	Map<String, ConversationHandle> store;

	ConversationStore() {
		this.store = new HashMap<String, ConversationHandle>();
	}

	public ConversationHandle getConverstation(String id) {
		return this.store.get(id);
	}

	public ConversationHandle addConverstation(Conversation conversation) {
		var id = UUID.randomUUID().toString();
		var handle = new ConversationHandle(id, conversation);

		this.store.put(id, handle);

		return handle;
	}

	public QueryBuilder createQueryBuilder() {
		return new QueryBuilder(store);
	}

	public static class QueryBuilder {
		Map<String, ConversationHandle> store;
		int offset = 0;
		int limit = 100;

		QueryBuilder(Map<String, ConversationHandle> store) {
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

		public List<ConversationHandle> list() {
			return this.store.entrySet().stream().skip(this.offset).limit(this.limit).map(Map.Entry::getValue)
					.collect(Collectors.toList());
		}
	}
}