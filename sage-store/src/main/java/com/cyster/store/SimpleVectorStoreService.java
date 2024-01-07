package com.cyster.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Component;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StreamUtils;

@Component
public class SimpleVectorStoreService implements VectorStoreService {
    private String DIRECTORY = "/tmp/vector-store";
    
    // TODO use com.google.common.cache.LoadingCache or like
   private EmbeddingClient embeddingClient;
    
    private Map<String, SimpleVectorStore> stores = new HashMap<String, SimpleVectorStore>();
    
    SimpleVectorStoreService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    public Set<String> getStores() {
        return stores.keySet();
    }
    
    public SimpleVectorStore getStore(String name) {
        if (stores.containsKey(name)) {
            return stores.get(name);
        }
            
        var store = new SimpleVectorStore(embeddingClient);
        
        this.stores.put(name, store);
        
        File file = new File(DIRECTORY + "/" + name);
        if (file.exists()) {
            store.load(file);
        }
        
        return store;
    }  
  
    public  void saveStore(String name) {
        if (!stores.containsKey(name)) {
            throw new RuntimeException("Store not found: " + name);
        }
        SimpleVectorStore store = stores.get(name);
        
        File file = new File(DIRECTORY + "/" + name);
        Path parent = Paths.get(file.getParent());
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create store directory");
            }
        }
        
        store.save(file);
    }
    
    
    public void buildStore(String name, String prefixUrl, String prefixPath) {  
        SimpleVectorStore store = this.getStore(name);
        
        List<Document> documents = new ArrayList<Document>();
        try {
            Path files = Paths.get(prefixPath);

            Files.walk(files)
                 .filter(Files::isRegularFile)
                 .forEach(path -> documents.addAll(new TextFileReader(path, prefixUrl, prefixPath).get()));
            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        store.add(documents);
        saveStore(name);
    }

    
    class TextFileReader implements DocumentReader {
        public static final String CHARSET_METADATA = "charset";
        public static final String SOURCE_METADATA = "source";

        private final String source;
        private final Path path;
        private Charset charset = StandardCharsets.UTF_8;

        public TextFileReader(Path path, String prefixUrl, String prefixPath) {
            String resourceUrl = path.toAbsolutePath().toString();
            if (!resourceUrl.startsWith(prefixPath)) {
                throw new RuntimeException("Resource not in: " + prefixPath);
            }
            this.source = resourceUrl.replace(prefixPath, prefixUrl);
            
            this.path = path;
        }

        public Charset getCharset() {
            return this.charset;
        }

        @Override
        public List<Document> get() {
            try (InputStream inputStream = Files.newInputStream(this.path)) {
                String document = StreamUtils.copyToString(inputStream, this.charset);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put(CHARSET_METADATA, this.charset.name());
                metadata.put(SOURCE_METADATA, this.source);

                return List.of(new Document(document, metadata));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
