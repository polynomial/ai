package com.cyster.ai.vector;

/*
public class XVectorStoreService {

    private ChromaApi chromaApi;
    private Map<String, VectorStore> stores = new HashMap<String, VectorStore>();
    
    VectorStoreService(EmbeddingClient embeddingClient, ChromaApi chromaApi) {
        this.embeddingClient = embeddingClient;
        this.chromaApi = chromaApi;
    }
    
    public VectorStore getStore(String name) {
        if (stores.containsKey(name)) {
            return stores.get(name);
        }
        
        var store = new ChromaVectorStore(this.embeddingClient, this.chromaApi, name);
        
        try {
            store.afterPropertiesSet();  // TBD
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        this.stores.put(name, store);
        
        return store;
    }
    
}
*/