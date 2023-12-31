package com.extole.insight.advisors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.cyster.ai.vector.store.SimpleVectorStoreService;


@Component
public class ExtoleCodeVectorStore {
    private SimpleVectorStore simpleVectorStore;
    private File storeFile;
    
    public ExtoleCodeVectorStore(SimpleVectorStoreService simpleVectorStoreService) {
        this.simpleVectorStore = simpleVectorStoreService.getStore("extole-documents"); 
        this.storeFile = new File("/tmp/extole-code-store.json");
        
        createStore();
    }
      
    private void loadStore() {
        if (this.storeFile.exists()) {
            this.simpleVectorStore.load(this.storeFile);
        } else {
            this.createStore();
            this.simpleVectorStore.save(this.storeFile);
        }
    }
    
    private void createStore() {   
        List<Document> documents = new ArrayList<Document>();
        try {
            Path files = Paths.get(System.getenv("AI_HOME"), "/extole/github.com");

            System.out.println("files");
            Files.walk(files)
                 .filter(Files::isRegularFile)
                 .forEach(file -> System.out.println(file));
            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        this.simpleVectorStore.add(documents);
    }

    void load(String sourceFile) {
        JsonReader jsonReader = new JsonReader(new FileSystemResource(sourceFile),
                "price", "name", "shortDescription", "description", "tags");
        List<Document> documents = jsonReader.get();
        this.simpleVectorStore.add(documents);
    }
}
