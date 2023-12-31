package com.cyster.ai.vector.store;

/*
import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.MilvusVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;

@Component
public class MilvusVectorStoreService implements VectorStoreService {
    private EmbeddingClient embeddingClient;
    
    private Map<String, VectorStore> stores = new HashMap<String, VectorStore>();
    
    MilvusVectorStoreService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }
    
    public VectorStore getStore(String name) {
        if (stores.containsKey(name)) {
            return stores.get(name);
        }
        
        MilvusServiceClient milvusClient = new MilvusServiceClient(
            ConnectParam.newBuilder()
              .withHost("localhost")
              .withPort(19530)
              .build()
          );
        
        // TODO specify database name... (currently its default)
        var store = new MilvusVectorStore(milvusClient, embeddingClient);
        
        this.stores.put(name, store);
        
        return store;
    }  
    
}
*/
