package com.cyster.ai.weave.service.scenario;

import java.io.IOException;
import java.io.InputStream;

public interface VectorStore {
    
    static interface Document {
        String getName();
        InputStream getInputStream() throws IOException;
    }
    
    static interface Builder {
        Builder withName(String name);
        Builder addDocument(String name, String contents);
        Builder addDocument(Document document);
        Id<VectorStore> create();
    }
}
