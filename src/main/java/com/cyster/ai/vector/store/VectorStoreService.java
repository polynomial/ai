package com.cyster.ai.vector.store;

import org.springframework.ai.vectorstore.VectorStore;

public interface VectorStoreService {
    VectorStore getStore(String name);
}
