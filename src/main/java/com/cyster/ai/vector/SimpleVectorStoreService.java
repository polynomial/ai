package com.cyster.ai.vector;


import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;


@Component
public class SimpleVectorStoreService implements VectorStoreService {
    private EmbeddingClient embeddingClient;
    
    private Map<String, VectorStore> stores = new HashMap<String, VectorStore>();
    
    SimpleVectorStoreService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }
    
    public VectorStore getStore(String name) {
        if (stores.containsKey(name)) {
            return stores.get(name);
        }
            
        var store = new SimpleVectorStore(embeddingClient);
        
        this.stores.put(name, store);
        
        return store;
    }  
    
}
